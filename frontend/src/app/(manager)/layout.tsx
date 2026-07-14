'use client';

import React from 'react';
import { AuthGuard } from '@/components/auth/auth-guard';
import { Sidebar } from '@/components/layout/sidebar';
import { Header } from '@/components/layout/header';
import { 
  LayoutDashboard, 
  Calendar, 
  CheckSquare, 
  Package, 
  MessageSquare, 
  TrendingUp 
} from 'lucide-react';

export default function ManagerLayout({ children }: { children: React.ReactNode }) {
  const links = [
    { title: 'Dashboard', href: '/manager', icon: LayoutDashboard },
    { title: 'Ca làm & Phân công', href: '/manager/shifts', icon: Calendar },
    { title: 'Điểm danh chấm công', href: '/manager/attendance', icon: CheckSquare },
    { title: 'Quản lý kho nguyên liệu', href: '/manager/inventory', icon: Package },
    { title: 'Đánh giá khách hàng', href: '/manager/reviews', icon: MessageSquare },
    { title: 'Báo cáo doanh thu', href: '/manager/reports', icon: TrendingUp },
  ];

  const breadcrumbs = [
    { title: 'Quản trị', href: '/manager' },
    { title: 'Quản lý' },
  ];

  return (
    <AuthGuard requiredRole="MANAGER">
      <div className="flex min-h-screen bg-stone-50 text-stone-900">
        <Sidebar links={links} roleTitle="Manager Portal" roleBadge="warning" />
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
