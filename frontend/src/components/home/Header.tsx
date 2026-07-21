'use client';

import React, { useState } from 'react';
import Link from 'next/link';
import { usePathname, useRouter } from 'next/navigation';
import { useAuthStore } from '@/stores/auth';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { showToast } from '@/components/ui/toast';
import { CartBadge } from '@/components/cart/cart-badge';
import { Utensils, LogOut, LayoutDashboard, LogIn, UserPlus, Menu, X, ChevronRight } from 'lucide-react';
import { api } from '@/lib/api';

export function Header() {
  const router = useRouter();
  const pathname = usePathname();
  const { user, isAuthenticated, clearAuth, authStatus } = useAuthStore();
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);

  const handleLogout = async () => {
    try {
      await api.post('/auth/logout');
    } catch {
      // Ignore
    } finally {
      clearAuth();
      showToast.success('Đăng xuất thành công');
      setMobileMenuOpen(false);
      router.push('/login');
    }
  };

  const navItems = [
    { label: 'Trang chủ', href: '/' },
    { label: 'Thực đơn', href: '/menu' },
    { label: 'Món bán chạy', href: '/#best-sellers' },
    { label: 'Khuyến mãi', href: '/#promotions' },
    { label: 'Đánh giá', href: '/#reviews' },
  ];

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
        <Link href="/" className="flex items-center gap-3 group">
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
            const isActive = pathname === item.href;
            return (
              <Link 
                key={item.href} 
                href={item.href}
                className={`relative py-1 transition-colors hover:text-[#0F6B4F] ${
                  isActive ? 'text-[#0F6B4F]' : ''
                }`}
              >
                {item.label}
                <span className={`absolute bottom-0 left-0 w-full h-0.5 bg-[#0F6B4F] rounded-full transform origin-left transition-transform duration-300 ${
                  isActive ? 'scale-x-100' : 'scale-x-0 hover:scale-x-100'
                }`} />
              </Link>
            );
          })}
        </nav>

        {/* Right Actions (Cart, Auth, User & Mobile Toggle) */}
        <div className="flex items-center gap-3 sm:gap-4">
          
          {/* Cart Icon Badge Component */}
          <CartBadge />

          {/* User Auth Controls */}
          {authStatus === 'loading' ? (
            <div className="w-7 h-7 rounded-full border-2 border-stone-200 border-t-[#0F6B4F] animate-spin" />
          ) : isAuthenticated && user ? (
            <div className="hidden sm:flex items-center gap-3 pl-2 border-l border-[#E8E1D7]">
              <div className="text-right">
                <span className="block text-xs font-bold text-[#1F2937]">{user.fullName}</span>
                <Badge className="bg-[#ECFDF5] text-[#0F6B4F] border-[#A7F3D0] px-1.5 py-0 text-[9px] font-bold uppercase tracking-wider">
                  {user.roles[0]}
                </Badge>
              </div>

              <Link href={getDashboardUrl()}>
                <Button size="sm" variant="outline" className="h-9 px-3 border-[#0F6B4F] text-[#0F6B4F] hover:bg-[#0F6B4F] hover:text-white font-bold text-xs gap-1.5 rounded-xl transition-colors">
                  <LayoutDashboard size={14} />
                  <span>Quản lý</span>
                </Button>
              </Link>

              <Button 
                variant="ghost" 
                size="sm" 
                onClick={handleLogout} 
                className="h-9 w-9 p-0 text-stone-500 hover:text-red-600 hover:bg-red-50 rounded-xl"
                title="Đăng xuất"
              >
                <LogOut size={16} />
              </Button>
            </div>
          ) : (
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
                key={item.href}
                href={item.href}
                onClick={() => setMobileMenuOpen(false)}
                className="flex items-center justify-between p-3 rounded-xl hover:bg-[#FFF9F0] text-sm font-bold text-[#1F2937] hover:text-[#0F6B4F]"
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
