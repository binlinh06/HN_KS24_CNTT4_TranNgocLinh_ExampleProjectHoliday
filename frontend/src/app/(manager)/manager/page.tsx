'use client';

import React from 'react';
import { Card, CardHeader, CardContent } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { PageHeader } from '@/components/ui/page-header';
import { mockStats } from '@/mocks/dashboard';
import { 
  TrendingUp, 
  Calendar, 
  Package, 
  Star, 
  ArrowRight,
  ClipboardCheck,
  UserCheck
} from 'lucide-react';
import Link from 'next/link';

export default function ManagerDashboard() {
  const stats = mockStats.manager;

  const inventoryAlerts = [
    { name: 'Thịt bò phi lê', status: 'Sắp hết', quantity: '5.2 kg', minLimit: '15.0 kg' },
    { name: 'Bánh phở tươi', status: 'Sắp hết', quantity: '3.0 kg', minLimit: '10.0 kg' },
    { name: 'Hành hoa', status: 'Sắp hết', quantity: '0.8 kg', minLimit: '2.0 kg' },
  ];

  return (
    <div className="space-y-6">
      <PageHeader
        title="Bảng điều khiển Quản lý"
        description="Theo dõi hoạt động kinh doanh nhà hàng, điểm danh nhân viên, trạng thái kho nguyên liệu và đánh giá từ khách hàng."
      />

      {/* Stats Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 sm:gap-6">
        <Card hover>
          <CardContent className="p-6 flex items-center justify-between">
            <div className="space-y-1">
              <span className="text-xs font-semibold text-stone-500 uppercase">Doanh thu tháng này</span>
              <span className="block text-2xl font-bold text-stone-900">
                {stats.monthlyRevenue.toLocaleString('vi-VN')}đ
              </span>
            </div>
            <div className="w-11 h-11 rounded-full bg-primary-50 flex items-center justify-center text-primary-600 border border-primary-100">
              <TrendingUp size={20} />
            </div>
          </CardContent>
        </Card>

        <Card hover>
          <CardContent className="p-6 flex items-center justify-between">
            <div className="space-y-1">
              <span className="text-xs font-semibold text-stone-500 uppercase">Tỷ lệ đi làm (Hôm nay)</span>
              <span className="block text-2xl font-bold text-stone-900">
                {stats.attendanceRate}
              </span>
            </div>
            <div className="w-11 h-11 rounded-full bg-green-50 flex items-center justify-center text-green-600 border border-green-100">
              <UserCheck size={20} />
            </div>
          </CardContent>
        </Card>

        <Card hover className="border-l-4 border-l-error-500">
          <CardContent className="p-6 flex items-center justify-between">
            <div className="space-y-1">
              <span className="text-xs font-semibold text-stone-500 uppercase">Cảnh báo kho hàng</span>
              <span className="block text-2xl font-bold text-error-600">
                {stats.inventoryAlerts} nguyên liệu
              </span>
            </div>
            <div className="w-11 h-11 rounded-full bg-error-50 flex items-center justify-center text-error-600 border border-error-100">
              <Package size={20} />
            </div>
          </CardContent>
        </Card>

        <Card hover>
          <CardContent className="p-6 flex items-center justify-between">
            <div className="space-y-1">
              <span className="text-xs font-semibold text-stone-500 uppercase">Đánh giá trung bình</span>
              <span className="block text-2xl font-bold text-stone-900 flex items-center gap-1">
                {stats.averageRating}
                <Star size={18} fill="currentColor" className="text-amber-400 border-none" />
              </span>
            </div>
            <div className="w-11 h-11 rounded-full bg-accent-50 flex items-center justify-center text-accent-600 border border-accent-100">
              <Star size={20} />
            </div>
          </CardContent>
        </Card>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Left column: Inventory alert list */}
        <Card className="lg:col-span-2">
          <CardHeader className="flex flex-row justify-between items-center">
            <h3 className="text-base font-bold text-stone-900">Cảnh báo hết nguyên liệu</h3>
            <Link href="/manager/inventory">
              <Button variant="ghost" size="sm" className="text-primary-600 text-xs gap-1.5 p-0">
                Quản lý kho
                <ArrowRight size={14} />
              </Button>
            </Link>
          </CardHeader>
          <CardContent className="p-0">
            <div className="divide-y divide-stone-100">
              {inventoryAlerts.map((item, index) => (
                <div key={index} className="p-4 flex items-center justify-between hover:bg-stone-50/40 transition-colors">
                  <div className="space-y-1">
                    <span className="font-semibold text-stone-850 text-sm">{item.name}</span>
                    <div className="flex items-center gap-3 text-xs text-stone-500">
                      <span>Hiện tại: <strong className="text-stone-700">{item.quantity}</strong></span>
                      <span>Mức tối thiểu: <strong className="text-stone-700">{item.minLimit}</strong></span>
                    </div>
                  </div>
                  <Badge variant="error">{item.status}</Badge>
                </div>
              ))}
            </div>
          </CardContent>
        </Card>

        {/* Right column: Shift status & actions */}
        <div className="space-y-6">
          <Card>
            <CardHeader>
              <h3 className="text-sm font-bold text-stone-900">Quản trị nhân viên</h3>
            </CardHeader>
            <CardContent className="space-y-3">
              <Link href="/manager/shifts" className="block">
                <Button variant="outline" className="w-full justify-start gap-2 text-xs">
                  <Calendar size={14} />
                  Phân lịch ca làm việc
                </Button>
              </Link>
              <Link href="/manager/attendance" className="block">
                <Button variant="outline" className="w-full justify-start gap-2 text-xs">
                  <ClipboardCheck size={14} />
                  Duyệt bảng điểm danh
                </Button>
              </Link>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <h3 className="text-sm font-bold text-stone-900">Báo cáo nhanh tháng</h3>
            </CardHeader>
            <CardContent className="text-xs text-stone-600 space-y-2">
              <div className="flex justify-between py-1 border-b border-stone-100">
                <span>Số ca làm đã xếp</span>
                <span className="font-bold text-stone-800">120 ca</span>
              </div>
              <div className="flex justify-between py-1 border-b border-stone-100">
                <span>Số phản hồi mới</span>
                <span className="font-bold text-stone-850">14 phản hồi</span>
              </div>
              <div className="flex justify-between py-1">
                <span>Số mặt hàng nhập kho</span>
                <span className="font-bold text-stone-800">1,240 kg</span>
              </div>
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  );
}
