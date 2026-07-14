'use client';

import React, { useEffect, useState } from 'react';
import Link from 'next/link';
import { usePathname } from 'next/navigation';
import { useAuthStore } from '@/stores/auth';
import { Button } from '@/components/ui/button';
import { Utensils, LogOut, ChevronRight } from 'lucide-react';

export default function CustomerLayout({ children }: { children: React.ReactNode }) {
  const pathname = usePathname();
  const { user, isAuthenticated, initialize, clearAuth } = useAuthStore();
  const [checking, setChecking] = useState(true);

  useEffect(() => {
    initialize();
    setChecking(false);
  }, [initialize]);

  useEffect(() => {
    if (!checking) {
      if (!isAuthenticated || !user) {
        window.location.href = '/login';
      } else if (!user.roles.includes('CUSTOMER')) {
        window.location.href = '/unauthorized';
      }
    }
  }, [checking, isAuthenticated, user]);

  if (checking || !user || !user.roles.includes('CUSTOMER')) {
    return (
      <div className="flex items-center justify-center min-h-screen bg-stone-50">
        <div className="w-10 h-10 animate-spin rounded-full border-3 border-stone-200 border-t-pho-red" />
      </div>
    );
  }

  const handleLogout = () => {
    clearAuth();
    window.location.href = '/login';
  };

  const links = [{"title":"Dashboard","href":"/customer"},{"title":"Thông tin cá nhân","href":"/customer/profile"},{"title":"Địa chỉ giao hàng","href":"/customer/addresses"},{"title":"Giỏ hàng của tôi","href":"/customer/cart"},{"title":"Lịch sử đơn hàng","href":"/customer/orders"},{"title":"Đánh giá & Phản hồi","href":"/customer/reviews"}];

  return (
    <div className="flex min-h-screen bg-stone-50 text-stone-900">
      {/* Sidebar */}
      <aside className="w-64 bg-stone-900 text-stone-300 flex flex-col border-r border-stone-800 shrink-0">
        <div className="h-16 px-6 border-b border-stone-800 flex items-center gap-2.5">
          <div className="w-9 h-9 rounded-lg bg-pho-red flex items-center justify-center text-white font-bold">
            <Utensils size={18} />
          </div>
          <div>
            <span className="font-bold text-white text-sm tracking-tight block">PHỞ BÒ</span>
            <span className="text-[9px] text-pho-400 font-bold uppercase tracking-widest block -mt-1">Customer Portal</span>
          </div>
        </div>

        <nav className="flex-1 py-6 px-4 space-y-1.5 overflow-y-auto">
          {links.map((link) => {
            const isActive = pathname === link.href;
            return (
              <Link
                key={link.href}
                href={link.href}
                className={`flex items-center justify-between px-3 py-2.5 rounded-lg text-sm font-medium transition-all ... ${
                  isActive
                    ? 'bg-pho-red text-white shadow-md shadow-red-700/10'
                    : 'text-stone-400 hover:bg-stone-800 hover:text-white'
                }`}
              >
                <span className="flex items-center gap-2.5">
                  {link.title}
                </span>
                <ChevronRight size={14} className={isActive ? 'opacity-100' : 'opacity-30'} />
              </Link>
            );
          })}
        </nav>

        <div className="p-4 border-t border-stone-800 bg-stone-950/40">
          <div className="flex items-center gap-3 mb-4">
            <div className="w-9 h-9 rounded-full bg-stone-800 flex items-center justify-center text-stone-400 border border-stone-700 font-semibold text-sm">
              {user.fullName.charAt(0)}
            </div>
            <div className="min-w-0 flex-1">
              <span className="block text-xs font-semibold text-white truncate">{user.fullName}</span>
              <span className="block text-[9px] text-stone-500 font-bold uppercase truncate">{user.roles[0]}</span>
            </div>
          </div>
          <Button variant="ghost" className="w-full text-stone-400 hover:bg-stone-800 hover:text-white justify-start" size="sm" onClick={handleLogout}>
            <LogOut size={16} className="mr-2" />
            Đăng xuất
          </Button>
        </div>
      </aside>

      {/* Main Area */}
      <div className="flex-1 flex flex-col min-h-screen">
        {/* Header bar */}
        <header className="h-16 bg-white border-b border-stone-200 px-8 flex items-center justify-between shrink-0">
          <div className="flex items-center gap-2 text-xs text-stone-500 font-medium">
            <span>Bảng điều khiển</span>
            <ChevronRight size={12} />
            <span className="text-stone-800 font-semibold">Customer</span>
          </div>
          <div className="flex items-center gap-4">
            <span className="inline-flex items-center px-2 py-0.5 rounded text-[10px] font-bold uppercase tracking-wider bg-pho-red/10 text-pho-red border border-pho-red/20">
              Giai đoạn 1 - Khởi tạo
            </span>
          </div>
        </header>

        {/* Content */}
        <main className="flex-1 p-8 overflow-y-auto max-w-7xl w-full mx-auto">
          {children}
        </main>
      </div>
    </div>
  );
}
