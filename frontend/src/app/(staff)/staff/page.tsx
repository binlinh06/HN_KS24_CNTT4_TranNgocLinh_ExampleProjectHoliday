'use client';

import React from 'react';
import { Card, CardHeader, CardContent } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { PageHeader } from '@/components/ui/page-header';
import { mockOrders, mockStats } from '@/mocks/dashboard';
import { 
  ClipboardList, 
  ChefHat, 
  CheckCircle, 
  Grid, 
  ArrowRight,
  Monitor
} from 'lucide-react';
import Link from 'next/link';

export default function StaffDashboard() {
  const stats = mockStats.staff;

  return (
    <div className="space-y-6">
      <PageHeader
        title="Bảng điều khiển Nhân viên"
        description="Quản lý nhanh các hoạt động POS bán hàng, phục vụ bàn và hàng chờ chế biến trong bếp."
      />

      {/* Stats Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 sm:gap-6">
        <Card hover>
          <CardContent className="p-6 flex items-center justify-between">
            <div className="space-y-1">
              <span className="text-xs font-semibold text-stone-500 uppercase">Đơn chờ xác nhận</span>
              <span className="block text-2xl font-bold text-stone-900">
                {stats.pendingOrders} đơn
              </span>
            </div>
            <div className="w-11 h-11 rounded-full bg-primary-50 flex items-center justify-center text-primary-600 border border-primary-100">
              <ClipboardList size={20} />
            </div>
          </CardContent>
        </Card>

        <Card hover>
          <CardContent className="p-6 flex items-center justify-between">
            <div className="space-y-1">
              <span className="text-xs font-semibold text-stone-500 uppercase">Bếp đang nấu</span>
              <span className="block text-2xl font-bold text-accent-700">
                {stats.cookingOrders} đơn
              </span>
            </div>
            <div className="w-11 h-11 rounded-full bg-accent-50 flex items-center justify-center text-accent-600 border border-accent-100">
              <ChefHat size={20} />
            </div>
          </CardContent>
        </Card>

        <Card hover>
          <CardContent className="p-6 flex items-center justify-between">
            <div className="space-y-1">
              <span className="text-xs font-semibold text-stone-500 uppercase">Hoàn thành hôm nay</span>
              <span className="block text-2xl font-bold text-stone-900">
                {stats.completedToday} đơn
              </span>
            </div>
            <div className="w-11 h-11 rounded-full bg-green-50 flex items-center justify-center text-green-600 border border-green-100">
              <CheckCircle size={20} />
            </div>
          </CardContent>
        </Card>

        <Card hover>
          <CardContent className="p-6 flex items-center justify-between">
            <div className="space-y-1">
              <span className="text-xs font-semibold text-stone-500 uppercase">Bàn đang có khách</span>
              <span className="block text-2xl font-bold text-stone-900">
                {stats.activeTables} bàn
              </span>
            </div>
            <div className="w-11 h-11 rounded-full bg-stone-100 flex items-center justify-center text-stone-600 border border-stone-200">
              <Grid size={20} />
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Quick Action Navigation Buttons */}
      <Card>
        <CardHeader>
          <h3 className="text-base font-bold text-stone-900">Tính năng phục vụ</h3>
        </CardHeader>
        <CardContent className="grid grid-cols-2 md:grid-cols-4 gap-4">
          <Link href="/staff/pos">
            <Button className="w-full justify-center gap-2 h-12 bg-primary-600 hover:bg-primary-700 font-bold">
              <Monitor size={16} />
              Mở quầy POS
            </Button>
          </Link>
          <Link href="/staff/orders">
            <Button variant="outline" className="w-full justify-center gap-2 h-12">
              <ClipboardList size={16} />
              Xử lý đơn hàng
            </Button>
          </Link>
          <Link href="/staff/kitchen">
            <Button variant="outline" className="w-full justify-center gap-2 h-12">
              <ChefHat size={16} />
              Màn hình bếp
            </Button>
          </Link>
          <Link href="/staff/tables">
            <Button variant="outline" className="w-full justify-center gap-2 h-12">
              <Grid size={16} />
              Đặt bàn ăn
            </Button>
          </Link>
        </CardContent>
      </Card>

      {/* Processing Table */}
      <Card>
        <CardHeader className="flex flex-row justify-between items-center">
          <h3 className="text-base font-bold text-stone-900">Các đơn cần xử lý ngay</h3>
          <Link href="/staff/orders">
            <Button variant="ghost" size="sm" className="text-primary-600 text-xs gap-1.5 p-0">
              Xem toàn bộ danh sách
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
                <th className="px-6 py-3.5">Chi tiết món</th>
                <th className="px-6 py-3.5">Thành tiền</th>
                <th className="px-6 py-3.5">Trạng thái</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-stone-150">
              {mockOrders.slice(0, 3).map((order) => (
                <tr key={order.id} className="hover:bg-stone-50/50 transition-colors">
                  <td className="px-6 py-4 font-bold text-stone-950">{order.id}</td>
                  <td className="px-6 py-4 font-medium text-stone-850">{order.customerName}</td>
                  <td className="px-6 py-4 text-stone-500 truncate max-w-[250px]">{order.items}</td>
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
