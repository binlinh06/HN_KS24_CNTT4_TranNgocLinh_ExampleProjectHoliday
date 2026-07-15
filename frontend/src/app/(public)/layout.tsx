'use client';

import React from 'react';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { useAuthStore } from '@/stores/auth';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { showToast } from '@/components/ui/toast';
import { CartBadge } from '@/components/cart/cart-badge';
import { Utensils, LogOut, LayoutDashboard, LogIn, UserPlus } from 'lucide-react';
import { api } from '@/lib/api';

export default function PublicLayout({ children }: { children: React.ReactNode }) {
  const router = useRouter();
  const { user, isAuthenticated, clearAuth, authStatus } = useAuthStore();

  const handleLogout = async () => {
    try {
      await api.post('/auth/logout');
    } catch (e) {
      // Ignored
    } finally {
      clearAuth();
      showToast.success('Đăng xuất thành công');
      router.push('/login');
    }
  };

  return (
    <div className="flex flex-col min-h-screen bg-stone-50">
      {/* Header */}
      <header className="sticky top-0 z-40 bg-white/90 backdrop-blur-md border-b border-stone-200">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between">
          <Link href="/" className="flex items-center gap-2.5 group">
            <div className="w-10 h-10 rounded-xl bg-primary-500 flex items-center justify-center text-white shadow-sm shadow-primary-950/20 group-hover:bg-primary-600 transition-colors">
              <Utensils size={20} className="text-white" />
            </div>
            <div>
              <span className="font-bold text-stone-900 tracking-tight block text-sm sm:text-base">
                PHỞ BÒ GIA TRUYỀN
              </span>
              <span className="text-[9px] text-stone-500 uppercase tracking-widest block font-bold -mt-0.5">
                Hương vị truyền thống Việt Nam
              </span>
            </div>
          </Link>

          <nav className="hidden md:flex items-center gap-6 text-sm font-medium text-stone-600">
            <Link href="/" className="hover:text-primary-600 transition-colors">Trang chủ</Link>
            <Link href="/menu" className="hover:text-primary-600 transition-colors">Thực đơn</Link>
            <Link href="/#best-sellers" className="hover:text-primary-600 transition-colors">Món bán chạy</Link>
            <Link href="/#reviews" className="hover:text-primary-600 transition-colors">Đánh giá</Link>
          </nav>

          {/* Cart Badge */}
          <CartBadge />

          <div className="flex items-center gap-3">
            {authStatus === 'loading' ? (
              <div className="w-8 h-8 rounded-full border-2 border-stone-200 border-t-primary-500 animate-spin" />
            ) : isAuthenticated && user ? (
              <div className="flex items-center gap-2 sm:gap-4">
                <div className="hidden sm:block text-right">
                  <span className="block text-xs font-bold text-stone-850">{user.fullName}</span>
                  <span className="inline-block">
                    <Badge variant="primary" className="px-1.5 py-0 text-[8px] uppercase tracking-wider font-bold">
                      {user.roles[0]}
                    </Badge>
                  </span>
                </div>
                <Link href={
                  user.roles.includes('ADMIN') ? '/admin' : 
                  user.roles.includes('MANAGER') ? '/manager' : 
                  user.roles.includes('STAFF') ? '/staff' : '/customer'
                }>
                  <Button variant="outline" size="sm" className="gap-1.5">
                    <LayoutDashboard size={14} />
                    <span className="hidden sm:inline">Dashboard</span>
                  </Button>
                </Link>
                <Button variant="ghost" size="sm" onClick={handleLogout} className="text-stone-500 hover:text-red-600">
                  <LogOut size={16} />
                </Button>
              </div>
            ) : (
              <>
                <Link href="/login">
                  <Button variant="ghost" size="sm" className="gap-1.5 text-stone-600 hover:text-primary-600">
                    <LogIn size={15} />
                    Đăng nhập
                  </Button>
                </Link>
                <Link href="/register">
                  <Button size="sm" className="gap-1.5 bg-primary-500 hover:bg-primary-600">
                    <UserPlus size={15} />
                    Đăng ký
                  </Button>
                </Link>
              </>
            )}
          </div>
        </div>
      </header>

      {/* Main Content */}
      <main className="flex-1">
        {children}
      </main>

      {/* Footer */}
      <footer className="bg-stone-900 text-stone-400 border-t border-stone-800">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
          <div className="grid grid-cols-1 md:grid-cols-4 gap-8">
            <div className="space-y-4">
              <div className="flex items-center gap-2.5">
                <div className="w-8 h-8 rounded-lg bg-primary-500 flex items-center justify-center text-white font-bold">
                  <Utensils size={16} className="text-white" />
                </div>
                <span className="font-bold text-white text-sm tracking-tight">PHỞ BÒ GIA TRUYỀN</span>
              </div>
              <p className="text-xs leading-relaxed text-stone-400">
                Mang đến hương vị phở bò gia truyền chuẩn vị Hà Nội cổ xưa, nước dùng ngọt thanh từ xương, sợi bánh dai mềm và thịt bò tuyển chọn tươi ngon mỗi ngày.
              </p>
            </div>
            
            <div>
              <h4 className="text-white font-bold text-sm mb-4 tracking-wide">LIÊN KẾT NHANH</h4>
              <ul className="space-y-2 text-xs">
                <li><Link href="/" className="hover:text-white transition-colors">Trang chủ</Link></li>
                <li><Link href="/#categories" className="hover:text-white transition-colors">Danh mục món ăn</Link></li>
                <li><Link href="/#best-sellers" className="hover:text-white transition-colors">Món bán chạy</Link></li>
                <li><Link href="/register" className="hover:text-white transition-colors">Đăng ký thành viên</Link></li>
              </ul>
            </div>

            <div>
              <h4 className="text-white font-bold text-sm mb-4 tracking-wide">GIỜ MỞ CỬA</h4>
              <ul className="space-y-2 text-xs text-stone-450">
                <li>Thứ 2 - Thứ Sáu: 06:00 - 22:00</li>
                <li>Thứ 7 - Chủ Nhật: 06:00 - 23:00</li>
                <li>Thời gian giao hàng: 06:30 - 21:30</li>
              </ul>
            </div>

            <div>
              <h4 className="text-white font-bold text-sm mb-4 tracking-wide">LIÊN HỆ</h4>
              <ul className="space-y-2 text-xs text-stone-450">
                <li>Địa chỉ: 123 Đường Láng, Đống Đa, Hà Nội</li>
                <li>Hotline đặt bàn: 1900 1234</li>
                <li>Email: contact@phobogiatruyen.vn</li>
              </ul>
            </div>
          </div>

          <div className="border-t border-stone-850 mt-12 pt-6 text-center text-xs text-stone-500">
            <p>© {new Date().getFullYear()} Phở Bò Gia Truyền Management System. Giai đoạn 4 – Giỏ hàng & Mã giảm giá.</p>
          </div>
        </div>
      </footer>
    </div>
  );
}
