'use client';

import React from 'react';
import { Card, CardHeader, CardContent } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { PageHeader } from '@/components/ui/page-header';
import { mockOrders, mockStats } from '@/mocks/dashboard';
import { 
  Award, 
  CreditCard, 
  ShoppingBag, 
  MapPin, 
  ArrowRight,
  Utensils
} from 'lucide-react';
import Link from 'next/link';
import { useAuthStore } from '@/stores/auth';

export default function CustomerDashboard() {
  const { user } = useAuthStore();
  const stats = mockStats.customer;
  
  // Filter only online orders for customers
  const customerOrders = mockOrders.filter(o => o.type === 'ONLINE').slice(0, 3);

  return (
    <div className="space-y-6">
      <PageHeader
        title={`Xin chào, ${user?.fullName || 'Khách hàng'}`}
        description="Chào mừng bạn đến với trang quản lý tài khoản thành viên của Phở Bò Gia Truyền."
      />

      {/* Stats row */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 sm:gap-6">
        <Card hover className="border-l-4 border-l-primary-500">
          <CardContent className="p-6 flex items-center justify-between">
            <div className="space-y-1">
              <span className="text-xs font-semibold text-stone-500 uppercase">Điểm tích lũy</span>
              <span className="block text-2xl font-bold text-primary-700">
                {stats.loyaltyPoints} điểm
              </span>
            </div>
            <div className="w-11 h-11 rounded-full bg-primary-50 flex items-center justify-center text-primary-600">
              <Award size={20} />
            </div>
          </CardContent>
        </Card>

        <Card hover className="border-l-4 border-l-accent-500">
          <CardContent className="p-6 flex items-center justify-between">
            <div className="space-y-1">
              <span className="text-xs font-semibold text-stone-500 uppercase">Đã chi tiêu</span>
              <span className="block text-2xl font-bold text-stone-900">
                {stats.totalSpent.toLocaleString('vi-VN')}đ
              </span>
            </div>
            <div className="w-11 h-11 rounded-full bg-accent-50 flex items-center justify-center text-accent-600">
              <CreditCard size={20} />
            </div>
          </CardContent>
        </Card>

        <Card hover>
          <CardContent className="p-6 flex items-center justify-between">
            <div className="space-y-1">
              <span className="text-xs font-semibold text-stone-500 uppercase">Tổng đơn hàng</span>
              <span className="block text-2xl font-bold text-stone-900">
                {stats.ordersCount} đơn
              </span>
            </div>
            <div className="w-11 h-11 rounded-full bg-stone-100 flex items-center justify-center text-stone-600">
              <ShoppingBag size={20} />
            </div>
          </CardContent>
        </Card>

        <Card hover>
          <CardContent className="p-6 flex items-center justify-between">
            <div className="space-y-1">
              <span className="text-xs font-semibold text-stone-500 uppercase">Sổ địa chỉ</span>
              <span className="block text-2xl font-bold text-stone-900">
                {stats.savedAddresses} địa chỉ
              </span>
            </div>
            <div className="w-11 h-11 rounded-full bg-stone-100 flex items-center justify-center text-stone-600">
              <MapPin size={20} />
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Main sections */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Left Column: Recent Orders */}
        <Card className="lg:col-span-2">
          <CardHeader className="flex flex-row justify-between items-center">
            <h3 className="text-base font-bold text-stone-900">Đơn hàng gần đây</h3>
            <Link href="/customer/orders">
              <Button variant="ghost" size="sm" className="text-primary-600 text-xs gap-1.5 p-0">
                Xem lịch sử
                <ArrowRight size={14} />
              </Button>
            </Link>
          </CardHeader>
          <CardContent className="p-0">
            {customerOrders.length > 0 ? (
              <div className="divide-y divide-stone-100">
                {customerOrders.map((order) => (
                  <div key={order.id} className="p-5 flex items-center justify-between hover:bg-stone-50/40 transition-colors">
                    <div className="space-y-1">
                      <div className="flex items-center gap-2">
                        <span className="font-bold text-stone-900 text-sm">{order.id}</span>
                        <span className="text-[10px] text-stone-400 font-semibold">{order.time}</span>
                      </div>
                      <p className="text-xs text-stone-500">{order.items}</p>
                      <p className="text-xs font-bold text-stone-850">
                        {order.total.toLocaleString('vi-VN')}đ
                      </p>
                    </div>
                    <div>
                      <Badge
                        variant={
                          order.status === 'HOAN_THANH' ? 'success' :
                          order.status === 'DA_HUY' ? 'error' :
                          order.status === 'DANG_CHE_BIEN' ? 'warning' : 'primary'
                        }
                      >
                        {order.status}
                      </Badge>
                    </div>
                  </div>
                ))}
              </div>
            ) : (
              <div className="p-8 text-center text-stone-450 text-sm">Bạn chưa đặt đơn hàng nào.</div>
            )}
          </CardContent>
        </Card>

        {/* Right Column: Quick order CTA & Loyalty status */}
        <div className="space-y-6">
          <Card className="bg-primary-700 text-white relative overflow-hidden">
            <div className="absolute inset-0 bg-[radial-gradient(#C7A45B_1px,transparent_1px)] [background-size:16px_16px] opacity-15" />
            <CardContent className="p-6 relative z-10 space-y-4">
              <h3 className="font-bold text-lg text-white">Thèm phở nóng hổi?</h3>
              <p className="text-xs text-primary-100 leading-relaxed">
                Đặt hàng trực tuyến giao nhanh trong 30 phút. Tô phở của bạn sẽ luôn nóng hổi và đúng chuẩn hương vị gia truyền.
              </p>
              <Link href="/" className="block">
                <Button size="sm" className="w-full bg-accent-500 hover:bg-accent-600 text-white font-bold gap-1.5">
                  <Utensils size={14} />
                  Đặt phở ngay
                </Button>
              </Link>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <h3 className="text-sm font-bold text-stone-900">Thông tin đặc quyền</h3>
            </CardHeader>
            <CardContent className="space-y-3 text-xs text-stone-600">
              <div className="flex justify-between items-center py-1.5 border-b border-stone-100">
                <span>Ưu đãi sinh nhật</span>
                <Badge variant="accent">Giảm 20%</Badge>
              </div>
              <div className="flex justify-between items-center py-1.5 border-b border-stone-100">
                <span>Freeship bán kính 3km</span>
                <Badge variant="success">Kích hoạt</Badge>
              </div>
              <div className="flex justify-between items-center py-1.5">
                <span>Hạng thành viên</span>
                <Badge variant="primary" className="font-bold">BẠC</Badge>
              </div>
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  );
}
