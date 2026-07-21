'use client';

import React, { useState } from 'react';
import { PageHeader } from '@/components/ui/page-header';
import { useStaffOrders, acceptStaffOrder, rejectStaffOrder, serveStaffOrder, handoverStaffOrder } from '@/features/staff/api';
import { useStaffSSE } from '@/features/staff/hooks/useStaffSSE';
import { useQueryClient } from '@tanstack/react-query';
import { staffKeys } from '@/features/staff/api';
import { toast } from 'react-hot-toast';
import Link from 'next/link';

export default function StaffOrdersPage() {
  // Realtime updates
  useStaffSSE('orders');

  const queryClient = useQueryClient();
  const [statusFilter, setStatusFilter] = useState<string>('');
  const [typeFilter, setTypeFilter] = useState<string>('');
  const [actionLoading, setActionLoading] = useState<string | null>(null);
  const [rejectReasonMap, setRejectReasonMap] = useState<Record<string, string>>({});
  const [showRejectModal, setShowRejectModal] = useState<string | null>(null);

  const { data: orders = [], isLoading, error } = useStaffOrders({
    status: statusFilter || undefined,
    type: typeFilter || undefined,
  });

  const handleAction = async (orderId: string, actionName: string, actionFn: (id: string) => Promise<any>) => {
    setActionLoading(`${orderId}-${actionName}`);
    try {
      await actionFn(orderId);
      toast.success('Thao tác thành công');
      queryClient.invalidateQueries({ queryKey: staffKeys.all });
    } catch (err: any) {
      toast.error(err.response?.data?.message || 'Thao tác thất bại');
    } finally {
      setActionLoading(null);
    }
  };

  const handleReject = async (orderId: string) => {
    const reason = rejectReasonMap[orderId] || '';
    if (!reason.trim()) {
      toast.error('Vui lòng nhập lý do từ chối');
      return;
    }
    setActionLoading(`${orderId}-reject`);
    try {
      await rejectStaffOrder(orderId, reason);
      toast.success('Từ chối đơn hàng thành công');
      setShowRejectModal(null);
      queryClient.invalidateQueries({ queryKey: staffKeys.all });
    } catch (err: any) {
      toast.error(err.response?.data?.message || 'Có lỗi xảy ra');
    } finally {
      setActionLoading(null);
    }
  };

  const getStatusBadge = (status: string) => {
    switch (status) {
      case 'CHO_XAC_NHAN':
        return <span className="px-2.5 py-1 text-xs font-semibold rounded-full bg-amber-50 text-amber-700 border border-amber-200">Chờ xác nhận</span>;
      case 'DA_XAC_NHAN':
        return <span className="px-2.5 py-1 text-xs font-semibold rounded-full bg-blue-50 text-blue-700 border border-blue-200">Đã xác nhận</span>;
      case 'DANG_CHE_BIEN':
        return <span className="px-2.5 py-1 text-xs font-semibold rounded-full bg-indigo-50 text-indigo-700 border border-indigo-200 animate-pulse">Đang chế biến</span>;
      case 'DANG_PHUC_VU':
        return <span className="px-2.5 py-1 text-xs font-semibold rounded-full bg-purple-50 text-purple-700 border border-purple-200">Đang phục vụ</span>;
      case 'DANG_GIAO':
        return <span className="px-2.5 py-1 text-xs font-semibold rounded-full bg-orange-50 text-orange-700 border border-orange-200">Đang giao</span>;
      case 'HOAN_THANH':
        return <span className="px-2.5 py-1 text-xs font-semibold rounded-full bg-emerald-50 text-emerald-700 border border-emerald-200">Hoàn thành</span>;
      case 'DA_HUY':
        return <span className="px-2.5 py-1 text-xs font-semibold rounded-full bg-red-50 text-red-700 border border-red-200">Đã hủy</span>;
      default:
        return <span className="px-2.5 py-1 text-xs font-semibold rounded-full bg-gray-50 text-gray-700">{status}</span>;
    }
  };

  return (
    <div className="space-y-6">
      <PageHeader
        title="Quản lý đơn hàng"
        description="Xem danh sách đơn hàng POS & Online, xác nhận đơn hàng mới và xử lý bàn giao, phục vụ."
      />

      {/* Filter bar */}
      <div className="flex flex-col sm:flex-row gap-4 p-4 bg-white rounded-xl border border-gray-200 shadow-sm">
        <div className="flex-1 flex flex-col gap-1">
          <label className="text-xs font-semibold text-gray-500 uppercase">Trạng thái</label>
          <select
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value)}
            className="w-full px-3 py-2 border border-gray-300 rounded-lg shadow-sm focus:outline-none focus:ring-1 focus:ring-emerald-500"
          >
            <option value="">Tất cả trạng thái</option>
            <option value="CHO_XAC_NHAN">Chờ xác nhận</option>
            <option value="DA_XAC_NHAN">Đã xác nhận</option>
            <option value="DANG_CHE_BIEN">Đang chế biến</option>
            <option value="DANG_PHUC_VU">Đang phục vụ</option>
            <option value="DANG_GIAO">Đang giao</option>
            <option value="HOAN_THANH">Hoàn thành</option>
            <option value="DA_HUY">Đã hủy</option>
          </select>
        </div>

        <div className="flex-1 flex flex-col gap-1">
          <label className="text-xs font-semibold text-gray-500 uppercase">Loại đơn</label>
          <select
            value={typeFilter}
            onChange={(e) => setTypeFilter(e.target.value)}
            className="w-full px-3 py-2 border border-gray-300 rounded-lg shadow-sm focus:outline-none focus:ring-1 focus:ring-emerald-500"
          >
            <option value="">Tất cả loại đơn</option>
            <option value="ONLINE">Đơn hàng Online</option>
            <option value="POS">Đơn hàng POS</option>
          </select>
        </div>
      </div>

      {isLoading ? (
        <div className="flex justify-center py-12">
          <div className="animate-spin rounded-full h-10 w-10 border-t-2 border-b-2 border-emerald-600"></div>
        </div>
      ) : error ? (
        <div className="p-4 bg-red-50 text-red-700 border border-red-200 rounded-xl text-center">
          Lỗi tải danh sách đơn hàng. Vui lòng thử lại.
        </div>
      ) : orders.length === 0 ? (
        <div className="p-6 bg-white rounded-xl border border-gray-200 shadow-sm text-center py-12">
          <p className="text-gray-500">Không có đơn hàng nào khớp với bộ lọc.</p>
        </div>
      ) : (
        <div className="bg-white rounded-xl border border-gray-200 shadow-sm overflow-hidden">
          <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse">
              <thead>
                <tr className="bg-gray-50 border-b border-gray-200 text-xs font-bold text-gray-500 uppercase tracking-wider">
                  <th className="p-4">Mã đơn hàng</th>
                  <th className="p-4">Thời gian</th>
                  <th className="p-4">Loại đơn</th>
                  <th className="p-4">Thanh toán</th>
                  <th className="p-4">Tổng tiền</th>
                  <th className="p-4">Trạng thái</th>
                  <th className="p-4 text-right">Thao tác</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-200 text-sm text-gray-700">
                {orders.map((order) => {
                  const isBusy = actionLoading?.startsWith(order.orderId);
                  const isDineIn = order.tableNumber !== null;

                  return (
                    <tr key={order.orderId} className="hover:bg-gray-50">
                      <td className="p-4">
                        <Link 
                          href={`/staff/orders/${order.orderId}`}
                          className="font-bold text-emerald-700 hover:underline"
                        >
                          {order.orderCode}
                        </Link>
                        {isDineIn && (
                          <div className="text-xs text-gray-500 font-semibold">
                            Bàn: {order.tableNumber}
                          </div>
                        )}
                      </td>
                      <td className="p-4 text-xs text-gray-500">
                        {new Date(order.createdAt).toLocaleString('vi-VN')}
                      </td>
                      <td className="p-4 font-semibold text-xs text-gray-600">
                        {order.paymentMethod === 'ONLINE_GATEWAY' ? 'ONLINE' : 'POS'}
                      </td>
                      <td className="p-4 text-xs">
                        <div className="font-semibold text-gray-700">{order.paymentMethod}</div>
                        <div className={`font-bold ${order.paymentStatus === 'SUCCESS' ? 'text-emerald-600' : 'text-amber-600'}`}>
                          {order.paymentStatus}
                        </div>
                      </td>
                      <td className="p-4 font-bold text-gray-900">
                        {order.finalAmount.toLocaleString('vi-VN')}đ
                      </td>
                      <td className="p-4">
                        {getStatusBadge(order.status)}
                      </td>
                      <td className="p-4 text-right space-x-2">
                        {order.status === 'CHO_XAC_NHAN' && order.paymentMethod === 'COD' && (
                          <>
                            <button
                              onClick={() => handleAction(order.orderId, 'accept', acceptStaffOrder)}
                              disabled={isBusy}
                              className="px-3 py-1 bg-emerald-600 hover:bg-emerald-700 text-white rounded text-xs font-semibold shadow-sm disabled:opacity-50"
                            >
                              Nhận đơn
                            </button>
                            <button
                              onClick={() => setShowRejectModal(order.orderId)}
                              disabled={isBusy}
                              className="px-3 py-1 bg-red-50 hover:bg-red-100 text-red-700 border border-red-200 rounded text-xs font-semibold disabled:opacity-50"
                            >
                              Từ chối
                            </button>
                          </>
                        )}

                        {order.status === 'DANG_CHE_BIEN' && isDineIn && (
                          <button
                            onClick={() => handleAction(order.orderId, 'serve', serveStaffOrder)}
                            disabled={isBusy}
                            className="px-3 py-1 bg-emerald-600 hover:bg-emerald-700 text-white rounded text-xs font-semibold shadow-sm disabled:opacity-50"
                          >
                            Phục vụ
                          </button>
                        )}

                        {order.status === 'DANG_CHE_BIEN' && !isDineIn && (
                          <button
                            onClick={() => handleAction(order.orderId, 'handover', handoverStaffOrder)}
                            disabled={isBusy}
                            className="px-3 py-1 bg-emerald-600 hover:bg-emerald-700 text-white rounded text-xs font-semibold shadow-sm disabled:opacity-50"
                          >
                            Bàn giao
                          </button>
                        )}

                        <Link
                          href={`/staff/orders/${order.orderId}`}
                          className="inline-block px-3 py-1 bg-white border border-gray-300 text-gray-700 hover:bg-gray-50 rounded text-xs font-semibold"
                        >
                          Chi tiết
                        </Link>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* Reject Modal */}
      {showRejectModal && (
        <div className="fixed inset-0 bg-black bg-opacity-40 flex items-center justify-center p-4 z-50 animate-fadeIn">
          <div className="bg-white rounded-xl border border-gray-200 shadow-lg max-w-md w-full p-6 space-y-4">
            <h3 className="text-lg font-bold text-gray-900">Từ chối đơn hàng</h3>
            <div className="space-y-1">
              <label className="text-sm font-semibold text-gray-600">Lý do từ chối</label>
              <textarea
                value={rejectReasonMap[showRejectModal] || ''}
                onChange={(e) => setRejectReasonMap({
                  ...rejectReasonMap,
                  [showRejectModal]: e.target.value
                })}
                placeholder="Nhập lý do từ chối (ví dụ: hết nguyên liệu, ngoài giờ giao hàng...)"
                rows={3}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-1 focus:ring-red-500"
              />
            </div>
            <div className="flex justify-end gap-2">
              <button
                onClick={() => setShowRejectModal(null)}
                className="px-4 py-2 border border-gray-300 rounded-lg hover:bg-gray-50 text-sm font-semibold"
              >
                Hủy
              </button>
              <button
                onClick={() => handleReject(showRejectModal)}
                disabled={actionLoading === `${showRejectModal}-reject`}
                className="px-4 py-2 bg-red-600 hover:bg-red-700 text-white rounded-lg text-sm font-semibold shadow-sm"
              >
                Xác nhận từ chối
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
