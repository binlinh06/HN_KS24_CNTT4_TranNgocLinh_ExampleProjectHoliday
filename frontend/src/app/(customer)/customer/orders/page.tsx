'use client';

import React, { useState } from 'react';
import Link from 'next/link';
import { PageHeader } from '@/components/ui/page-header';
import { useOrdersHistory } from '@/features/orders/api';
import { StatusBadge } from '@/components/ui/status-badge';
import { LoadingSpinner } from '@/components/ui/loading-spinner';
import { EmptyState } from '@/components/ui/empty-state';
import { Pagination } from '@/components/ui/pagination';
import { Input } from '@/components/ui/input';
import { Select } from '@/components/ui/select';
import { Calendar, Search, SlidersHorizontal, ArrowRight, Eye, MessageSquareQuote } from 'lucide-react';

export default function CustomerOrdersHistoryPage() {
  const [page, setPage] = useState(1);
  const [status, setStatus] = useState<string>('');
  const [date, setDate] = useState<string>('');
  const [sortBy, setSortBy] = useState<string>('createdAt,desc');

  const { data, isLoading, error } = useOrdersHistory({
    page: page - 1, // backend is 0-indexed
    size: 5,
    status: status || undefined,
    date: date || undefined,
    sort: sortBy,
  });

  const handleStatusChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    setStatus(e.target.value);
    setPage(1);
  };

  const handleDateChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setDate(e.target.value);
    setPage(1);
  };

  const handleSortChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    setSortBy(e.target.value);
    setPage(1);
  };

  const formatPrice = (amount: number) => {
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(amount);
  };

  const formatDate = (dateStr: string) => {
    return new Date(dateStr).toLocaleString('vi-VN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  return (
    <div className="space-y-6 max-w-6xl mx-auto px-4 sm:px-6">
      <PageHeader
        title="Lịch sử & Theo dõi Đơn hàng"
        description="Quản lý và xem tiến độ chuẩn bị các đơn hàng phở bò của bạn theo thời gian thực."
      />

      {/* Filters Toolbar */}
      <div className="bg-white rounded-xl border border-gray-100 shadow-sm p-4 flex flex-col md:flex-row gap-4 items-center justify-between">
        <div className="flex flex-wrap items-center gap-3 w-full md:w-auto">
          <div className="relative min-w-[180px] flex-1 sm:flex-initial">
            <span className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400">
              <SlidersHorizontal className="h-4 w-4" />
            </span>
            <select
              value={status}
              onChange={handleStatusChange}
              className="pl-9 w-full bg-gray-50 border border-gray-200 rounded-lg text-sm px-3 py-2 text-gray-700 outline-none focus:border-[#0F6B4F] focus:ring-1 focus:ring-[#0F6B4F] transition"
            >
              <option value="">Tất cả trạng thái</option>
              <option value="CHO_XAC_NHAN">Chờ xác nhận</option>
              <option value="DA_XAC_NHAN">Đã xác nhận</option>
              <option value="DANG_CHE_BIEN">Đang chế biến</option>
              <option value="DANG_GIAO">Đang giao hàng</option>
              <option value="DANG_PHUC_VU">Đang phục vụ</option>
              <option value="HOAN_THANH">Hoàn thành</option>
              <option value="DA_HUY">Đã hủy</option>
            </select>
          </div>

          <div className="relative min-w-[180px] flex-1 sm:flex-initial">
            <span className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400">
              <Calendar className="h-4 w-4" />
            </span>
            <input
              type="date"
              value={date}
              onChange={handleDateChange}
              className="pl-9 w-full bg-gray-50 border border-gray-200 rounded-lg text-sm px-3 py-2 text-gray-700 outline-none focus:border-[#0F6B4F] focus:ring-1 focus:ring-[#0F6B4F] transition"
            />
          </div>
        </div>

        <div className="flex items-center gap-3 w-full md:w-auto">
          <span className="text-sm text-gray-500 whitespace-nowrap hidden sm:inline">Sắp xếp:</span>
          <select
            value={sortBy}
            onChange={handleSortChange}
            className="w-full md:w-auto bg-gray-50 border border-gray-200 rounded-lg text-sm px-3 py-2 text-gray-700 outline-none focus:border-[#0F6B4F] focus:ring-1 focus:ring-[#0F6B4F] transition"
          >
            <option value="createdAt,desc">Mới nhất trước</option>
            <option value="createdAt,asc">Cũ nhất trước</option>
            <option value="finalAmount,desc">Tổng tiền giảm dần</option>
            <option value="finalAmount,asc">Tổng tiền tăng dần</option>
          </select>
        </div>
      </div>

      {/* Orders List */}
      {isLoading ? (
        <div className="flex justify-center items-center py-20 bg-white rounded-xl border border-gray-100 shadow-sm">
          <LoadingSpinner size="lg" />
        </div>
      ) : error ? (
        <div className="bg-red-50 text-red-700 p-6 rounded-xl border border-red-100 text-center">
          Tải lịch sử đơn hàng thất bại. Vui lòng thử lại sau.
        </div>
      ) : !data?.content || data.content.length === 0 ? (
        <div className="bg-white rounded-xl border border-gray-100 shadow-sm p-12 text-center">
          <EmptyState
            title="Không tìm thấy đơn hàng"
            description="Bạn chưa thực hiện đơn hàng nào phù hợp với bộ lọc đã chọn."
          />
        </div>
      ) : (
        <div className="space-y-4">
          {data.content.map((order) => (
            <div
              key={order.orderId}
              className="bg-white rounded-xl border border-gray-100 shadow-sm hover:shadow-md transition duration-200 overflow-hidden"
            >
              <div className="p-5 sm:p-6 flex flex-col sm:flex-row sm:items-center justify-between gap-4">
                <div className="space-y-2">
                  <div className="flex flex-wrap items-center gap-2">
                    <span className="text-sm font-bold text-gray-800 tracking-wide bg-gray-100 px-2.5 py-1 rounded">
                      #{order.orderCode}
                    </span>
                    <span className="text-xs text-gray-400">
                      {formatDate(order.createdAt)}
                    </span>
                  </div>

                  <p className="text-sm text-gray-600 font-medium">
                    {order.itemPreview}
                  </p>

                  <div className="flex flex-wrap items-center gap-3 pt-1">
                    <div className="flex items-center gap-1.5">
                      <span className="text-xs text-gray-400">Trạng thái:</span>
                      <StatusBadge status={order.status} />
                    </div>
                    <div className="h-3 w-px bg-gray-200 hidden sm:block"></div>
                    <div className="flex items-center gap-1.5">
                      <span className="text-xs text-gray-400">Thanh toán:</span>
                      <StatusBadge status={order.paymentStatus} />
                    </div>
                  </div>
                </div>

                <div className="flex sm:flex-col items-start sm:items-end justify-between sm:justify-center gap-3 pt-3 sm:pt-0 border-t sm:border-0 border-gray-100">
                  <div className="text-left sm:text-right">
                    <div className="text-xs text-gray-400">Tổng thanh toán</div>
                    <div className="text-lg font-bold text-[#0F6B4F] sm:text-xl">
                      {formatPrice(order.finalAmount)}
                    </div>
                  </div>

                  <div className="flex items-center gap-2">
                    {order.status === 'HOAN_THANH' && order.canReview && (
                      <Link
                        href={`/customer/orders/${order.orderId}?writeReview=true`}
                        className="inline-flex items-center gap-1 text-xs font-semibold text-[#C7A45B] hover:text-[#b08e49] bg-amber-50 hover:bg-amber-100 px-3 py-1.5 rounded-lg transition"
                      >
                        <MessageSquareQuote className="h-3.5 w-3.5" />
                        Đánh giá
                      </Link>
                    )}
                    <Link
                      href={`/customer/orders/${order.orderId}`}
                      className="inline-flex items-center gap-1 text-xs font-semibold text-white bg-[#0F6B4F] hover:bg-[#0c5942] px-3.5 py-1.5 rounded-lg shadow-sm transition"
                    >
                      <Eye className="h-3.5 w-3.5" />
                      Chi tiết
                    </Link>
                  </div>
                </div>
              </div>
            </div>
          ))}

          {/* Pagination */}
          {data.page && data.page.totalPages > 1 && (
            <div className="flex justify-center pt-4">
              <Pagination
                currentPage={page}
                totalPages={data.page.totalPages}
                onPageChange={(p) => setPage(p)}
              />
            </div>
          )}
        </div>
      )}
    </div>
  );
}
