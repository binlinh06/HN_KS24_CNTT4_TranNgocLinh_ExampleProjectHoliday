'use client';

import React, { useState, useEffect } from 'react';
import Link from 'next/link';
import { usePathname, useRouter } from 'next/navigation';
import { useAuthStore } from '@/stores/auth';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { showToast } from '@/components/ui/toast';
import { CartBadge } from '@/components/cart/cart-badge';
import { 
  Utensils, LogOut, LayoutDashboard, LogIn, UserPlus, Menu, X, 
  ChevronRight, User, ShoppingBag, ChevronDown 
} from 'lucide-react';
import { api } from '@/lib/api';

export function Header() {
  const router = useRouter();
  const pathname = usePathname();
  const { user, isAuthenticated, clearAuth, authStatus } = useAuthStore();
  
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
  const [userDropdownOpen, setUserDropdownOpen] = useState(false);
  const [activeSection, setActiveSection] = useState<string>('home');

  // Handle Logout
  const handleLogout = async () => {
    try {
      await api.post('/auth/logout');
    } catch {
      // Ignore
    } finally {
      clearAuth();
      showToast.success('Đăng xuất thành công');
      setMobileMenuOpen(false);
      setUserDropdownOpen(false);
      router.push('/login');
    }
  };

  // Nav items definition
  const navItems = [
    { label: 'Trang chủ', id: 'home', href: '/' },
    { label: 'Thực đơn', id: 'menu', href: '/menu' },
    { label: 'Món bán chạy', id: 'best-sellers', href: '/#best-sellers' },
    { label: 'Khuyến mãi', id: 'promotions', href: '/#promotions' },
    { label: 'Đánh giá', id: 'reviews', href: '/#reviews' },
  ];

  // Active section tracking via IntersectionObserver & Hash URL
  useEffect(() => {
    if (pathname !== '/') {
      if (pathname.startsWith('/menu')) {
        setActiveSection('menu');
      } else {
        setActiveSection('');
      }
      return;
    }

    // On homepage, check initial hash or default to 'home'
    const currentHash = typeof window !== 'undefined' ? window.location.hash.replace('#', '') : '';
    if (currentHash && ['home', 'best-sellers', 'promotions', 'reviews'].includes(currentHash)) {
      setActiveSection(currentHash);
    } else {
      setActiveSection('home');
    }

    const sectionIds = ['home', 'best-sellers', 'promotions', 'reviews'];
    const observerCallback: IntersectionObserverCallback = (entries) => {
      entries.forEach((entry) => {
        if (entry.isIntersecting) {
          setActiveSection(entry.target.id);
        }
      });
    };

    const observerOptions: IntersectionObserverInit = {
      root: null,
      rootMargin: '-20% 0px -60% 0px',
      threshold: 0.2,
    };

    const observer = new IntersectionObserver(observerCallback, observerOptions);

    sectionIds.forEach((id) => {
      const el = document.getElementById(id);
      if (el) observer.observe(el);
    });

    return () => {
      sectionIds.forEach((id) => {
        const el = document.getElementById(id);
        if (el) observer.unobserve(el);
      });
    };
  }, [pathname]);

  // Smooth scroll handler for hash links
  const handleNavClick = (e: React.MouseEvent, item: typeof navItems[0]) => {
    setMobileMenuOpen(false);

    if (item.href === '/menu') {
      return; // Standard next/link navigation
    }

    if (pathname === '/') {
      e.preventDefault();
      const targetId = item.id;
      const element = document.getElementById(targetId);

      if (element) {
        element.scrollIntoView({ behavior: 'smooth', block: 'start' });
        window.history.pushState(null, '', item.href === '/' ? '/' : `#${targetId}`);
        setActiveSection(targetId);
      } else if (item.id === 'home') {
        window.scrollTo({ top: 0, behavior: 'smooth' });
        window.history.pushState(null, '', '/');
        setActiveSection('home');
      }
    }
  };

  const getDashboardUrl = () => {
    if (!user || !user.roles) return '/customer';
    if (user.roles.includes('ADMIN')) return '/admin';
    if (user.roles.includes('MANAGER')) return '/manager';
    if (user.roles.includes('STAFF')) return '/staff';
    return '/customer';
  };

  return (
    <header className="sticky top-0 z-50 bg-[#FFFCF7]/95 backdrop-blur-md border-b border-[#E8E1D7] transition-all">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-20 flex items-center justify-between">
        
        {/* Brand Logo Left */}
        <Link href="/" className="flex items-center gap-3 group" onClick={(e) => handleNavClick(e, navItems[0])}>
          <div className="w-11 h-11 rounded-2xl bg-[#0F6B4F] flex items-center justify-center text-white shadow-md shadow-[#0F6B4F]/20 group-hover:bg-[#084C38] transition-colors">
            <Utensils size={22} className="text-white" />
          </div>
          <div>
            <span className="font-extrabold text-[#1F2937] tracking-tight block text-base sm:text-lg leading-none group-hover:text-[#0F6B4F] transition-colors">
              PHỞ BÒ GIA TRUYỀN
            </span>
            <span className="text-[10px] text-[#C7A45B] uppercase tracking-widest block font-bold mt-1">
              Hương vị truyền thống Việt Nam
            </span>
          </div>
        </Link>

        {/* Center Navigation Links (Desktop) */}
        <nav className="hidden md:flex items-center gap-7 text-sm font-bold text-[#1F2937]">
          {navItems.map((item) => {
            const isActive = activeSection === item.id;
            return (
              <Link 
                key={item.id} 
                href={item.href}
                onClick={(e) => handleNavClick(e, item)}
                className={`relative py-1 transition-colors hover:text-[#0F6B4F] ${
                  isActive ? 'text-[#0F6B4F]' : 'text-[#1F2937]'
                }`}
              >
                {item.label}
                <span className={`absolute bottom-0 left-0 w-full h-0.5 bg-[#0F6B4F] rounded-full transform origin-left transition-transform duration-300 ${
                  isActive ? 'scale-x-100' : 'scale-x-0 group-hover:scale-x-100'
                }`} />
              </Link>
            );
          })}
        </nav>

        {/* Right Actions (Cart, Auth Controls & Mobile Toggle) */}
        <div className="flex items-center gap-3 sm:gap-4">
          
          {/* Cart Icon Badge Component */}
          <CartBadge />

          {/* User Auth Controls */}
          {authStatus === 'loading' ? (
            /* Tiny Spinner during actual auth initialization only */
            <div className="w-7 h-7 rounded-full border-2 border-stone-200 border-t-[#0F6B4F] animate-spin" />
          ) : isAuthenticated && user ? (
            /* Authenticated User Menu Dropdown */
            <div className="relative">
              <button
                onClick={() => setUserDropdownOpen(!userDropdownOpen)}
                className="hidden sm:flex items-center gap-2.5 p-1.5 pr-3 rounded-2xl border border-[#E8E1D7] hover:border-[#0F6B4F] bg-white transition-all shadow-sm"
              >
                <div className="w-8 h-8 rounded-xl bg-[#0F6B4F] text-white flex items-center justify-center font-bold text-xs">
                  {user.fullName ? user.fullName.charAt(0).toUpperCase() : 'U'}
                </div>
                <div className="text-left text-xs font-bold text-[#1F2937]">
                  <span className="block max-w-[110px] truncate leading-tight">{user.fullName}</span>
                  <span className="text-[9px] text-[#0F6B4F] uppercase tracking-wider block font-semibold">
                    {user.roles[0]}
                  </span>
                </div>
                <ChevronDown size={14} className="text-[#6B7280]" />
              </button>

              {/* Dropdown Box */}
              {userDropdownOpen && (
                <>
                  <div 
                    className="fixed inset-0 z-40" 
                    onClick={() => setUserDropdownOpen(false)} 
                  />
                  <div className="absolute right-0 mt-2 w-56 bg-white rounded-2xl border border-[#E8E1D7] shadow-xl z-50 py-2 space-y-1 animate-slide-down">
                    <div className="px-4 py-2.5 border-b border-[#E8E1D7]">
                      <p className="text-xs font-extrabold text-[#1F2937]">{user.fullName}</p>
                      <p className="text-[11px] text-[#6B7280] truncate">{user.email}</p>
                    </div>

                    <Link 
                      href="/customer/profile" 
                      onClick={() => setUserDropdownOpen(false)}
                      className="flex items-center gap-2.5 px-4 py-2 text-xs font-bold text-[#1F2937] hover:bg-[#FFF9F0] hover:text-[#0F6B4F]"
                    >
                      <User size={15} />
                      <span>Thông tin tài khoản</span>
                    </Link>

                    {user.roles.includes('CUSTOMER') && (
                      <Link 
                        href="/customer/orders" 
                        onClick={() => setUserDropdownOpen(false)}
                        className="flex items-center gap-2.5 px-4 py-2 text-xs font-bold text-[#1F2937] hover:bg-[#FFF9F0] hover:text-[#0F6B4F]"
                      >
                        <ShoppingBag size={15} />
                        <span>Đơn hàng của tôi</span>
                      </Link>
                    )}

                    <Link 
                      href={getDashboardUrl()} 
                      onClick={() => setUserDropdownOpen(false)}
                      className="flex items-center gap-2.5 px-4 py-2 text-xs font-bold text-[#0F6B4F] hover:bg-[#ECFDF5]"
                    >
                      <LayoutDashboard size={15} />
                      <span>Trang Quản Lý ({user.roles[0]})</span>
                    </Link>

                    <div className="border-t border-[#E8E1D7] pt-1">
                      <button
                        onClick={handleLogout}
                        className="w-full flex items-center gap-2.5 px-4 py-2 text-xs font-bold text-red-600 hover:bg-red-50 text-left"
                      >
                        <LogOut size={15} />
                        <span>Đăng xuất</span>
                      </button>
                    </div>
                  </div>
                </>
              )}
            </div>
          ) : (
            /* Unauthenticated Login / Register Buttons */
            <div className="hidden sm:flex items-center gap-2">
              <Link href="/login">
                <Button variant="ghost" size="sm" className="h-9 text-xs font-bold text-[#1F2937] hover:text-[#0F6B4F] hover:bg-[#FFF9F0] rounded-xl gap-1.5">
                  <LogIn size={15} />
                  Đăng nhập
                </Button>
              </Link>
              <Link href="/register">
                <Button size="sm" className="h-9 text-xs font-bold bg-[#0F6B4F] hover:bg-[#084C38] text-white rounded-xl gap-1.5 shadow-sm">
                  <UserPlus size={15} />
                  Đăng ký
                </Button>
              </Link>
            </div>
          )}

          {/* Mobile Hamburger Toggle */}
          <button
            onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
            className="md:hidden p-2 rounded-xl text-[#1F2937] hover:bg-[#FFF9F0] transition-colors"
            aria-label="Toggle Navigation Menu"
          >
            {mobileMenuOpen ? <X size={24} /> : <Menu size={24} />}
          </button>

        </div>
      </div>

      {/* Mobile Drawer Navigation Menu */}
      {mobileMenuOpen && (
        <div className="md:hidden bg-[#FFFCF7] border-b border-[#E8E1D7] px-4 pt-2 pb-6 space-y-4 animate-slide-down">
          <nav className="flex flex-col space-y-2">
            {navItems.map((item) => (
              <Link
                key={item.id}
                href={item.href}
                onClick={(e) => handleNavClick(e, item)}
                className={`flex items-center justify-between p-3 rounded-xl text-sm font-bold transition-colors ${
                  activeSection === item.id ? 'bg-[#ECFDF5] text-[#0F6B4F]' : 'hover:bg-[#FFF9F0] text-[#1F2937]'
                }`}
              >
                <span>{item.label}</span>
                <ChevronRight size={16} className="text-[#C7A45B]" />
              </Link>
            ))}
          </nav>

          <div className="pt-4 border-t border-[#E8E1D7] space-y-3">
            {isAuthenticated && user ? (
              <div className="space-y-3">
                <div className="flex items-center justify-between px-2">
                  <div>
                    <p className="text-xs font-bold text-[#1F2937]">{user.fullName}</p>
                    <p className="text-[10px] text-[#6B7280]">{user.email}</p>
                  </div>
                  <Badge className="bg-[#ECFDF5] text-[#0F6B4F] text-[9px] font-bold">
                    {user.roles[0]}
                  </Badge>
                </div>

                <Link href="/customer/profile" onClick={() => setMobileMenuOpen(false)} className="block">
                  <Button variant="outline" className="w-full h-10 border-[#E8E1D7] text-[#1F2937] font-bold text-xs gap-2 rounded-xl">
                    <User size={16} />
                    Thông tin tài khoản
                  </Button>
                </Link>

                <Link href={getDashboardUrl()} onClick={() => setMobileMenuOpen(false)} className="block">
                  <Button className="w-full h-10 bg-[#0F6B4F] text-white font-bold text-xs gap-2 rounded-xl">
                    <LayoutDashboard size={16} />
                    Trang Quản Lý ({user.roles[0]})
                  </Button>
                </Link>

                <Button 
                  onClick={handleLogout} 
                  variant="outline" 
                  className="w-full h-10 border-red-200 text-red-600 hover:bg-red-50 font-bold text-xs gap-2 rounded-xl"
                >
                  <LogOut size={16} />
                  Đăng xuất
                </Button>
              </div>
            ) : (
              <div className="grid grid-cols-2 gap-3">
                <Link href="/login" onClick={() => setMobileMenuOpen(false)}>
                  <Button variant="outline" className="w-full h-10 border-[#E8E1D7] text-[#1F2937] font-bold text-xs rounded-xl">
                    Đăng nhập
                  </Button>
                </Link>
                <Link href="/register" onClick={() => setMobileMenuOpen(false)}>
                  <Button className="w-full h-10 bg-[#0F6B4F] text-white font-bold text-xs rounded-xl">
                    Đăng ký
                  </Button>
                </Link>
              </div>
            )}
          </div>
        </div>
      )}
    </header>
  );
}
