'use client';

import React from 'react';
import { AuthGuard } from '@/components/auth/auth-guard';
import { Sidebar } from '@/components/layout/sidebar';
import { Header } from '@/components/layout/header';
import { 
  LayoutDashboard, 
  Monitor, 
  ClipboardList, 
  ChefHat, 
  Grid 
} from 'lucide-react';

export default function StaffLayout({ children }: { children: React.ReactNode }) {
  const links = [
    { title: 'Dashboard', href: '/staff', icon: LayoutDashboard },
    { title: 'Quầy POS bán hàng', href: '/staff/pos', icon: Monitor },
    { title: 'Quản lý đơn hàng', href: '/staff/orders', icon: ClipboardList },
    { title: 'Hàng chờ chế biến', href: '/staff/kitchen', icon: ChefHat },
    { title: 'Sơ đồ bàn ăn', href: '/staff/tables', icon: Grid },
  ];

  const breadcrumbs = [
    { title: 'Vận hành', href: '/staff' },
    { title: 'Nhân viên' },
  ];

  return (
    <AuthGuard requiredRole="STAFF">
      <div className="flex min-h-screen bg-stone-50 text-stone-900">
        <Sidebar links={links} roleTitle="Staff Portal" roleBadge="success" />
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
