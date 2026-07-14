'use client';

import React, { useEffect } from 'react';
import Link from 'next/link';
import { useAuthStore } from '@/stores/auth';
import { Button } from '@/components/ui/button';
import { Utensils } from 'lucide-react';

export default function PublicLayout({ children }: { children: React.ReactNode }) {
  const { user, isAuthenticated, initialize, clearAuth } = useAuthStore();

  useEffect(() => {
    initialize();
  }, [initialize]);

  const handleLogout = async () => {
    clearAuth();
    window.location.href = '/login';
  };

  return (
    <div className="flex flex-col min-h-screen">
      {/* Header */}
      <header className="sticky top-0 z-40 bg-white border-b border-stone-200">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between">
          <Link href="/" className="flex items-center gap-2.5">
            <div className="w-10 h-10 rounded-xl bg-pho-red flex items-center justify-center text-white shadow-md shadow-red-700/20">
              <Utensils size={20} />
            </div>
            <div>
              <span className="font-bold text-stone-900 tracking-tight block">PHỞ BÒ GIA TRUYỀN</span>
              <span className="text-[10px] text-stone-500 uppercase tracking-widest block font-semibold -mt-1">Hệ Thống Quản Lý & Bán Hàng</span>
            </div>
          </Link>

          <nav className="hidden md:flex items-center gap-6">
            <Link href="/" className="text-sm font-medium text-stone-600 hover:text-pho-red transition-colors">Trang chủ</Link>
            <Link href="/menu" className="text-sm font-medium text-stone-600 hover:text-pho-red transition-colors">Thực đơn</Link>
          </nav>

          <div className="flex items-center gap-3">
            {isAuthenticated && user ? (
              <div className="flex items-center gap-4">
                <div className="text-right">
                  <span className="block text-xs font-semibold text-stone-800">{user.fullName}</span>
                  <span className="block text-[10px] text-stone-500 uppercase font-bold tracking-wider">{user.roles[0]}</span>
                </div>
                <Link href={user.roles.includes('ADMIN') ? '/admin' : user.roles.includes('MANAGER') ? '/manager' : user.roles.includes('STAFF') ? '/staff' : '/customer'}>
                  <Button variant="outline" size="sm">Dashboard</Button>
                </Link>
                <Button variant="ghost" size="sm" onClick={handleLogout}>Đăng xuất</Button>
              </div>
            ) : (
              <>
                <Link href="/login">
                  <Button variant="ghost" size="sm">Đăng nhập</Button>
                </Link>
                <Link href="/register">
                  <Button size="sm">Đăng ký</Button>
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
      <footer className="bg-stone-900 text-stone-400 py-8 border-t border-stone-800">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 text-center text-xs">
          <p>© {new Date().getFullYear()} Phở Bò Gia Truyền Management. Giai đoạn 1 - Khởi tạo Khung dự án.</p>
        </div>
      </footer>
    </div>
  );
}
