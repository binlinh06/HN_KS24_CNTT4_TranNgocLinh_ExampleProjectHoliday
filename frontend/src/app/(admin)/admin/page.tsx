'use client';

import React from 'react';
import { Card, CardHeader, CardContent } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { PageHeader } from '@/components/ui/page-header';
import { mockOrders, mockStats } from '@/mocks/dashboard';
import { 
  TrendingUp, 
  ShoppingBag, 
  Users, 
  UtensilsCrossed, 
  ArrowRight,
  Plus,
  FileText
} from 'lucide-react';
import Link from 'next/link';

export default function AdminDashboard() {
  const stats = mockStats.admin;

  return (
    <div className="space-y-6">
      <PageHeader
        title="Bảng điều khiển Admin"
        description="Tổng quan hệ thống, doanh thu và các hoạt động vận hành thời gian thực."
      />

      {/* Stat Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 sm:gap-6">
        <Card hover>
          <CardContent className="p-6 flex items-center justify-between">
            <div className="space-y-1">
              <span className="text-xs font-semibold text-stone-500 uppercase">Doanh thu hôm nay</span>
              <span className="block text-xl sm:text-2xl font-bold text-stone-900">
                {stats.revenueToday.toLocaleString('vi-VN')}đ
              </span>
            </div>
            <div className="w-12 h-12 rounded-xl bg-primary-50 flex items-center justify-center text-primary-600 border border-primary-100">
              <TrendingUp size={22} />
            </div>
          </CardContent>
        </Card>

        <Card hover>
          <CardContent className="p-6 flex items-center justify-between">
            <div className="space-y-1">
              <span className="text-xs font-semibold text-stone-500 uppercase">Đơn hàng mới</span>
              <span className="block text-xl sm:text-2xl font-bold text-stone-900">
                {stats.ordersToday} đơn
              </span>
            </div>
            <div className="w-12 h-12 rounded-xl bg-accent-50 flex items-center justify-center text-accent-600 border border-accent-100">
              <ShoppingBag size={22} />
            </div>
          </CardContent>
        </Card>

        <Card hover>
          <CardContent className="p-6 flex items-center justify-between">
            <div className="space-y-1">
              <span className="text-xs font-semibold text-stone-500 uppercase">Tổng thành viên</span>
              <span className="block text-xl sm:text-2xl font-bold text-stone-900">
                {stats.activeUsers.toLocaleString('vi-VN')}
              </span>
            </div>
            <div className="w-12 h-12 rounded-xl bg-blue-50 flex items-center justify-center text-blue-600 border border-blue-100">
              <Users size={22} />
            </div>
          </CardContent>
        </Card>

        <Card hover>
          <CardContent className="p-6 flex items-center justify-between">
            <div className="space-y-1">
              <span className="text-xs font-semibold text-stone-500 uppercase">Món trong thực đơn</span>
              <span className="block text-xl sm:text-2xl font-bold text-stone-900">
                {stats.menuItems} món
              </span>
            </div>
            <div className="w-12 h-12 rounded-xl bg-stone-100 flex items-center justify-center text-stone-600 border border-stone-200">
              <UtensilsCrossed size={22} />
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Quick Action Grid */}
      <Card>
        <CardHeader>
          <h3 className="text-base font-bold text-stone-900">Thao tác nhanh hệ thống</h3>
        </CardHeader>
        <CardContent className="grid grid-cols-2 md:grid-cols-4 gap-4">
          <Link href="/admin/users">
            <Button variant="outline" className="w-full justify-start gap-2 h-11 text-xs sm:text-sm">
              <Plus size={16} />
              Thêm tài khoản
            </Button>
          </Link>
          <Link href="/admin/products">
            <Button variant="outline" className="w-full justify-start gap-2 h-11 text-xs sm:text-sm">
              <Plus size={16} />
              Thêm món mới
            </Button>
          </Link>
          <Link href="/admin/vouchers">
            <Button variant="outline" className="w-full justify-start gap-2 h-11 text-xs sm:text-sm">
              <Plus size={16} />
              Tạo mã giảm giá
            </Button>
          </Link>
          <Link href="/admin/reports">
            <Button variant="outline" className="w-full justify-start gap-2 h-11 text-xs sm:text-sm">
              <FileText size={16} />
              Xuất báo cáo
            </Button>
          </Link>
        </CardContent>
      </Card>

      {/* Recent Orders */}
      <Card>
        <CardHeader className="flex flex-row justify-between items-center">
          <h3 className="text-base font-bold text-stone-900">Đơn hàng vừa đặt</h3>
          <Link href="/admin/reports">
            <Button variant="ghost" size="sm" className="text-primary-600 text-xs gap-1.5 p-0">
              Xem báo cáo
              <ArrowRight size={14} />
            </Button>
          </Link>
        </CardHeader>
        <CardContent className="overflow-x-auto p-0">
          <table className="w-full min-w-[600px] text-left text-sm border-collapse">
            <thead>
              <tr className="bg-stone-50 border-b border-stone-200 text-stone-500 font-semibold">
                <th className="px-6 py-3.5">Mã đơn</th>
                <th className="px-6 py-3.5">Khách hàng / Bàn</th>
                <th className="px-6 py-3.5">Loại</th>
                <th className="px-6 py-3.5">Chi tiết món</th>
                <th className="px-6 py-3.5">Thành tiền</th>
                <th className="px-6 py-3.5">Trạng thái</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-stone-150">
              {mockOrders.map((order) => (
                <tr key={order.id} className="hover:bg-stone-50/50 transition-colors">
                  <td className="px-6 py-4 font-bold text-stone-950">{order.id}</td>
                  <td className="px-6 py-4 font-medium text-stone-850">{order.customerName}</td>
                  <td className="px-6 py-4">
                    <Badge variant={order.type === 'ONLINE' ? 'info' : 'accent'}>
                      {order.type}
                    </Badge>
                  </td>
                  <td className="px-6 py-4 text-stone-500 max-w-[200px] truncate">{order.items}</td>
                  <td className="px-6 py-4 font-semibold text-stone-900">
                    {order.total.toLocaleString('vi-VN')}đ
                  </td>
                  <td className="px-6 py-4">
                    <Badge
                      variant={
                        order.status === 'HOAN_THANH' ? 'success' :
                        order.status === 'DA_HUY' ? 'error' :
                        order.status === 'DANG_CHE_BIEN' ? 'warning' : 'primary'
                      }
                    >
                      {order.status}
                    </Badge>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </CardContent>
      </Card>
    </div>
  );
}
