'use client';

import React from 'react';
import { AuthGuard } from '@/components/auth/auth-guard';
import { Sidebar } from '@/components/layout/sidebar';
import { Header } from '@/components/layout/header';
import { 
  LayoutDashboard, 
  User, 
  MapPin, 
  ShoppingBag, 
  History, 
  Star 
} from 'lucide-react';

export default function CustomerLayout({ children }: { children: React.ReactNode }) {
  const links = [
    { title: 'Dashboard', href: '/customer', icon: LayoutDashboard },
    { title: 'Thông tin cá nhân', href: '/customer/profile', icon: User },
    { title: 'Địa chỉ giao hàng', href: '/customer/addresses', icon: MapPin },
    { title: 'Giỏ hàng của tôi', href: '/customer/cart', icon: ShoppingBag },
    { title: 'Lịch sử đơn hàng', href: '/customer/orders', icon: History },
    { title: 'Đánh giá & Phản hồi', href: '/customer/reviews', icon: Star },
  ];

  const breadcrumbs = [
    { title: 'Cá nhân', href: '/customer' },
    { title: 'Khách hàng' },
  ];

  return (
    <AuthGuard requiredRole="CUSTOMER">
      <div className="flex min-h-screen bg-stone-50 text-stone-900">
        <Sidebar links={links} roleTitle="Customer Portal" roleBadge="primary" />
        <div className="flex-1 flex flex-col min-h-screen overflow-x-hidden">
          <Header breadcrumbs={breadcrumbs} />
          <main className="flex-1 p-6 sm:p-8 overflow-y-auto max-w-7xl w-full mx-auto">
            {children}
          </main>
        </div>
      </div>
    </AuthGuard>
  );
}
