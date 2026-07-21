'use client';

import React from 'react';
import { PageHeader } from '@/components/ui/page-header';
import { useStaffTables, useKitchenQueue, useStaffOrders } from '@/features/staff/api';
import { useStaffSSE } from '@/features/staff/hooks/useStaffSSE';
import Link from 'next/link';

export default function ManagerOperationsPage() {
  // Realtime updates
  useStaffSSE('orders');
  useStaffSSE('kitchen');

  const { data: tables = [], isLoading: tablesLoading } = useStaffTables();
  const { data: kitchenItems = [], isLoading: kitchenLoading } = useKitchenQueue();
  const { data: orders = [], isLoading: ordersLoading } = useStaffOrders();

  const activeTablesCount = tables.filter(t => t.status === 'OCCUPIED').length;
  const cleaningTablesCount = tables.filter(t => t.status === 'CLEANING').length;
  
  const waitingItemsCount = kitchenItems.filter(item => item.status === 'CHO').length;
  const cookingItemsCount = kitchenItems.filter(item => item.status === 'DANG_NAU').length;

  const pendingOrdersCount = orders.filter(o => o.status === 'CHO_XAC_NHAN').length;

  return (
    <div className="space-y-6">
      <PageHeader
        title="Bảng điều khiển vận hành (Manager)"
        description="Giao diện giám sát hiệu suất hoạt động cửa hàng, trạng thái các bàn ăn và tình hình phục vụ món của nhà bếp."
      />

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        {/* Stat 1: Tables */}
        <div className="bg-white rounded-xl border border-gray-200 shadow-sm p-6 space-y-2">
          <h4 className="text-xs font-bold text-gray-400 uppercase tracking-wider">Trạng thái Bàn ăn</h4>
          <div className="flex justify-between items-baseline">
            <span className="text-3xl font-extrabold text-gray-900">{activeTablesCount} / {tables.length}</span>
            <span className="text-xs font-semibold text-amber-600 bg-amber-50 px-2 py-0.5 rounded">Đang hoạt động</span>
          </div>
          <p className="text-xs text-gray-500">
            Có {cleaningTablesCount} bàn đang dọn dẹp.
          </p>
          <div className="pt-2 border-t border-gray-100">
            <Link 
              href="/staff/tables"
              className="text-xs font-bold text-emerald-700 hover:underline"
            >
              Xem sơ đồ bàn ăn &rarr;
            </Link>
          </div>
        </div>

        {/* Stat 2: Kitchen */}
        <div className="bg-white rounded-xl border border-gray-200 shadow-sm p-6 space-y-2">
          <h4 className="text-xs font-bold text-gray-400 uppercase tracking-wider">Hàng đợi Nhà bếp</h4>
          <div className="flex justify-between items-baseline">
            <span className="text-3xl font-extrabold text-gray-900">{waitingItemsCount + cookingItemsCount}</span>
            <span className="text-xs font-semibold text-blue-600 bg-blue-50 px-2 py-0.5 rounded">Món ăn trong lò</span>
          </div>
          <p className="text-xs text-gray-500 font-semibold">
            {waitingItemsCount} món chờ nấu, {cookingItemsCount} món đang nấu.
          </p>
          <div className="pt-2 border-t border-gray-100">
            <Link 
              href="/staff/kitchen"
              className="text-xs font-bold text-emerald-700 hover:underline"
            >
              Mở màn hình KDS &rarr;
            </Link>
          </div>
        </div>

        {/* Stat 3: Orders accepting */}
        <div className="bg-white rounded-xl border border-gray-200 shadow-sm p-6 space-y-2">
          <h4 className="text-xs font-bold text-gray-400 uppercase tracking-wider">Đơn hàng Online</h4>
          <div className="flex justify-between items-baseline">
            <span className="text-3xl font-extrabold text-gray-900">{pendingOrdersCount}</span>
            <span className="text-xs font-semibold text-red-600 bg-red-50 px-2 py-0.5 rounded">Chờ xác nhận</span>
          </div>
          <p className="text-xs text-gray-500">
            Các đơn giao hàng/mạng lưới cần duyệt gấp.
          </p>
          <div className="pt-2 border-t border-gray-100">
            <Link 
              href="/staff/orders"
              className="text-xs font-bold text-emerald-700 hover:underline"
            >
              Quản lý danh sách đơn &rarr;
            </Link>
          </div>
        </div>
      </div>

      {/* Main override controls */}
      <div className="bg-white rounded-xl border border-gray-200 shadow-sm p-6 space-y-4">
        <h3 className="text-lg font-bold text-gray-950 border-b border-gray-150 pb-2">Quyền năng Quản lý</h3>
        <p className="text-sm text-gray-600">
          Trang này được bảo vệ bởi bộ lọc quyền của Quản lý cửa hàng (Manager). Bạn có thể xem các phím tắt nhanh để override trạng thái và hỗ trợ nhân viên phục vụ khi cao điểm.
        </p>

        <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-4 gap-4 pt-2">
          <Link
            href="/staff/pos"
            className="p-4 bg-emerald-50 hover:bg-emerald-100 border border-emerald-200 rounded-xl text-center transition-colors shadow-sm"
          >
            <span className="block font-bold text-emerald-800 text-sm">Giao diện POS</span>
            <span className="text-xs text-emerald-600 mt-1 block">Thu ngân Overrides</span>
          </Link>
          <Link
            href="/staff/tables"
            className="p-4 bg-blue-50 hover:bg-blue-100 border border-blue-200 rounded-xl text-center transition-colors shadow-sm"
          >
            <span className="block font-bold text-blue-800 text-sm">Sơ đồ bàn ăn</span>
            <span className="text-xs text-blue-600 mt-1 block">Dọn dẹp & Tạm ngưng</span>
          </Link>
          <Link
            href="/staff/kitchen"
            className="p-4 bg-amber-50 hover:bg-amber-100 border border-amber-200 rounded-xl text-center transition-colors shadow-sm"
          >
            <span className="block font-bold text-amber-800 text-sm">Bếp KDS</span>
            <span className="text-xs text-amber-600 mt-1 block">Hoàn thành món ăn</span>
          </Link>
          <Link
            href="/staff/orders"
            className="p-4 bg-indigo-50 hover:bg-indigo-100 border border-indigo-200 rounded-xl text-center transition-colors shadow-sm"
          >
            <span className="block font-bold text-indigo-800 text-sm">Duyệt & Giao hàng</span>
            <span className="text-xs text-indigo-600 mt-1 block">Hủy đơn & Hoàn tiền</span>
          </Link>
        </div>
      </div>
    </div>
  );
}
