'use client';

import React from 'react';
import { AuthGuard } from '@/components/auth/auth-guard';
import { Sidebar } from '@/components/layout/sidebar';
import { Header } from '@/components/layout/header';
import { 
  LayoutDashboard, 
  Users, 
  FolderOpen, 
  UtensilsCrossed, 
  Settings2, 
  Ticket, 
  Settings, 
  BarChart3 
} from 'lucide-react';

export default function AdminLayout({ children }: { children: React.ReactNode }) {
  const links = [
    { title: 'Dashboard', href: '/admin', icon: LayoutDashboard },
    { title: 'Tài khoản người dùng', href: '/admin/users', icon: Users },
    { title: 'Danh mục thực đơn', href: '/admin/categories', icon: FolderOpen },
    { title: 'Món ăn & Giá cả', href: '/admin/products', icon: UtensilsCrossed },
    { title: 'Nhóm tùy chọn món', href: '/admin/options', icon: Settings2 },
    { title: 'Mã khuyến mãi Voucher', href: '/admin/vouchers', icon: Ticket },
    { title: 'Cấu hình hệ thống', href: '/admin/configurations', icon: Settings },
    { title: 'Báo cáo tài chính', href: '/admin/reports', icon: BarChart3 },
  ];

  const breadcrumbs = [
    { title: 'Hệ thống', href: '/admin' },
    { title: 'Admin' },
  ];

  return (
    <AuthGuard requiredRole="ADMIN">
      <div className="flex min-h-screen bg-stone-50 text-stone-900">
        <Sidebar links={links} roleTitle="Admin Portal" roleBadge="error" />
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
