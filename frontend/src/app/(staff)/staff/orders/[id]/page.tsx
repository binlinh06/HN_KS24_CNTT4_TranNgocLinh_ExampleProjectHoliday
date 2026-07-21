'use client';

import React, { useState } from 'react';
import { useParams, useRouter } from 'next/navigation';
import { PageHeader } from '@/components/ui/page-header';
import { 
  useStaffOrderDetails, 
  acceptStaffOrder, 
  rejectStaffOrder, 
  serveStaffOrder, 
  handoverStaffOrder,
  useInvoiceByOrder
} from '@/features/staff/api';
import { useStaffSSE } from '@/features/staff/hooks/useStaffSSE';
import { useQueryClient } from '@tanstack/react-query';
import { staffKeys } from '@/features/staff/api';
import { toast } from 'react-hot-toast';

export default function StaffOrderDetailsPage() {
  const params = useParams();
  const router = useRouter();
  const orderId = params.id as string;

  // Realtime updates
  useStaffSSE('orders');

  const queryClient = useQueryClient();
  const { data: order, isLoading, error } = useStaffOrderDetails(orderId);
  const { data: invoice } = useInvoiceByOrder(orderId);

  const [actionLoading, setActionLoading] = useState<string | null>(null);
  const [rejectReason, setRejectReason] = useState<string>('');
  const [showRejectModal, setShowRejectModal] = useState<boolean>(false);

  const handleAction = async (actionName: string, actionFn: (id: string) => Promise<any>) => {
    setActionLoading(actionName);
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

  const handleReject = async () => {
    if (!rejectReason.trim()) {
      toast.error('Vui lòng nhập lý do từ chối');
      return;
    }
    setActionLoading('reject');
    try {
      await rejectStaffOrder(orderId, rejectReason);
      toast.success('Từ chối đơn hàng thành công');
      setShowRejectModal(false);
      queryClient.invalidateQueries({ queryKey: staffKeys.all });
    } catch (err: any) {
      toast.error(err.response?.data?.message || 'Có lỗi xảy ra');
    } finally {
      setActionLoading(null);
    }
  };

  const printInvoice = () => {
    if (invoice) {
      window.open(`/staff/invoices/${invoice.id}/print`, '_blank', 'width=600,height=800');
    }
  };

  if (isLoading) {
    return (
      <div className="flex justify-center py-12">
        <div className="animate-spin rounded-full h-10 w-10 border-t-2 border-b-2 border-emerald-600"></div>
      </div>
    );
  }

  if (error || !order) {
    return (
      <div className="p-4 bg-red-50 text-red-700 border border-red-200 rounded-xl text-center">
        Lỗi tải chi tiết đơn hàng hoặc không tìm thấy đơn hàng.
      </div>
    );
  }

  const isDineIn = order.tableNumber !== null;

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <button 
          onClick={() => router.push('/staff/orders')}
          className="text-sm font-semibold text-emerald-700 hover:underline flex items-center gap-1"
        >
          &larr; Quay lại danh sách
        </button>

        <div className="space-x-2">
          {order.status === 'CHO_XAC_NHAN' && order.paymentMethod === 'COD' && (
            <>
              <button
                onClick={() => handleAction('accept', acceptStaffOrder)}
                disabled={!!actionLoading}
                className="px-4 py-2 bg-emerald-600 hover:bg-emerald-700 text-white rounded-lg text-sm font-semibold shadow-sm disabled:opacity-50"
              >
                Nhận đơn
              </button>
              <button
                onClick={() => setShowRejectModal(true)}
                disabled={!!actionLoading}
                className="px-4 py-2 bg-red-50 hover:bg-red-100 text-red-700 border border-red-200 rounded-lg text-sm font-semibold disabled:opacity-50"
              >
                Từ chối đơn
              </button>
            </>
          )}

          {order.status === 'DANG_CHE_BIEN' && isDineIn && (
            <button
              onClick={() => handleAction('serve', serveStaffOrder)}
              disabled={!!actionLoading}
              className="px-4 py-2 bg-emerald-600 hover:bg-emerald-700 text-white rounded-lg text-sm font-semibold shadow-sm disabled:opacity-50"
            >
              Xác nhận phục vụ
            </button>
          )}

          {order.status === 'DANG_CHE_BIEN' && !isDineIn && (
            <button
              onClick={() => handleAction('handover', handoverStaffOrder)}
              disabled={!!actionLoading}
              className="px-4 py-2 bg-emerald-600 hover:bg-emerald-700 text-white rounded-lg text-sm font-semibold shadow-sm disabled:opacity-50"
            >
              Bàn giao đơn hàng
            </button>
          )}

          {invoice && (
            <button
              onClick={printInvoice}
              className="px-4 py-2 bg-amber-600 hover:bg-amber-700 text-white rounded-lg text-sm font-semibold shadow-sm"
            >
              In hóa đơn (K80)
            </button>
          )}
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Order Details Column */}
        <div className="lg:col-span-2 space-y-6">
          {/* Header Info */}
          <div className="p-6 bg-white rounded-xl border border-gray-200 shadow-sm space-y-4">
            <div className="flex items-center justify-between border-b border-gray-100 pb-4">
              <div>
                <span className="text-xs font-semibold text-gray-400 uppercase">Mã đơn hàng</span>
                <h2 className="text-xl font-bold text-gray-900">{order.orderCode}</h2>
              </div>
              <div className="text-right">
                <span className="text-xs font-semibold text-gray-400 uppercase">Trạng thái</span>
                <div>{order.status}</div>
              </div>
            </div>

            <div className="grid grid-cols-2 gap-4 text-sm">
              <div>
                <span className="text-gray-500">Khách hàng:</span>
                <p className="font-semibold text-gray-800">{order.receiverNameSnapshot || 'Khách vãng lai'}</p>
              </div>
              <div>
                <span className="text-gray-500">Số điện thoại:</span>
                <p className="font-semibold text-gray-800">{order.receiverPhoneSnapshot || 'N/A'}</p>
              </div>
              <div className="col-span-2">
                <span className="text-gray-500">Địa chỉ phục vụ/giao hàng:</span>
                <p className="font-semibold text-gray-800">
                  {isDineIn ? `Bàn số ${order.tableNumber}` : (order.shippingAddressSnapshot || 'Mua mang về (POS)')}
                </p>
              </div>
            </div>
          </div>

          {/* Items Table */}
          <div className="p-6 bg-white rounded-xl border border-gray-200 shadow-sm space-y-4">
            <h3 className="text-md font-bold text-gray-800">Chi tiết sản phẩm</h3>
            <div className="divide-y divide-gray-150">
              {order.items?.map((item: any) => (
                <div key={item.id} className="py-4 flex items-start justify-between">
                  <div className="space-y-1">
                    <h4 className="font-bold text-gray-900">{item.productNameSnapshot}</h4>
                    {item.options && item.options.length > 0 && (
                      <ul className="text-xs text-gray-500 list-disc pl-4 space-y-0.5">
                        {item.options.map((opt: any) => (
                          <li key={opt.id}>
                            {opt.optionGroupNameSnapshot}: {opt.optionNameSnapshot} (+{opt.incrementalPriceSnapshot.toLocaleString('vi-VN')}đ)
                          </li>
                        ))}
                      </ul>
                    )}
                    <span className="text-xs text-gray-400">Số lượng: {item.quantity}</span>
                  </div>
                  <div className="text-right">
                    <span className="font-bold text-gray-800">
                      {((item.basePriceSnapshot + item.optionsPriceSnapshot) * item.quantity).toLocaleString('vi-VN')}đ
                    </span>
                    <div className="text-xs text-gray-400">
                      {(item.basePriceSnapshot + item.optionsPriceSnapshot).toLocaleString('vi-VN')}đ / cái
                    </div>
                  </div>
                </div>
              ))}
            </div>

            <div className="border-t border-gray-200 pt-4 space-y-2 text-sm text-right">
              <div className="flex justify-between text-gray-500">
                <span>Tạm tính</span>
                <span>{order.subtotal.toLocaleString('vi-VN')}đ</span>
              </div>
              <div className="flex justify-between text-gray-500">
                <span>Giảm giá</span>
                <span>-{order.discountAmount.toLocaleString('vi-VN')}đ</span>
              </div>
              <div className="flex justify-between font-bold text-gray-950 text-base">
                <span>Tổng tiền</span>
                <span>{order.finalAmount.toLocaleString('vi-VN')}đ</span>
              </div>
            </div>
          </div>
        </div>

        {/* Timeline Column */}
        <div className="space-y-6">
          <div className="p-6 bg-white rounded-xl border border-gray-200 shadow-sm space-y-4">
            <h3 className="text-md font-bold text-gray-800">Thông tin thanh toán</h3>
            <div className="space-y-2 text-sm">
              <div className="flex justify-between">
                <span className="text-gray-500">Phương thức:</span>
                <span className="font-semibold text-gray-800">{order.paymentMethod}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-500">Trạng thái:</span>
                <span className={`font-bold ${order.paymentStatus === 'SUCCESS' ? 'text-emerald-600' : 'text-amber-600'}`}>
                  {order.paymentStatus}
                </span>
              </div>
            </div>
          </div>

          <div className="p-6 bg-white rounded-xl border border-gray-200 shadow-sm space-y-4">
            <h3 className="text-md font-bold text-gray-800">Lịch sử cập nhật</h3>
            <div className="relative border-l-2 border-emerald-500 pl-4 ml-2 space-y-6">
              {/* Timeline Items */}
              <div className="space-y-4">
                <div className="relative">
                  <div className="absolute -left-6 bg-emerald-500 rounded-full w-3.5 h-3.5 border-2 border-white"></div>
                  <div className="text-xs font-semibold text-gray-500">
                    {new Date(order.createdAt).toLocaleString('vi-VN')}
                  </div>
                  <p className="text-sm font-semibold text-gray-800">Đơn hàng được khởi tạo</p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Reject Modal */}
      {showRejectModal && (
        <div className="fixed inset-0 bg-black bg-opacity-40 flex items-center justify-center p-4 z-50">
          <div className="bg-white rounded-xl border border-gray-200 shadow-lg max-w-md w-full p-6 space-y-4">
            <h3 className="text-lg font-bold text-gray-900">Từ chối đơn hàng</h3>
            <div className="space-y-1">
              <label className="text-sm font-semibold text-gray-600">Lý do từ chối</label>
              <textarea
                value={rejectReason}
                onChange={(e) => setRejectReason(e.target.value)}
                placeholder="Nhập lý do từ chối..."
                rows={3}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-1 focus:ring-red-500"
              />
            </div>
            <div className="flex justify-end gap-2">
              <button
                onClick={() => setShowRejectModal(false)}
                className="px-4 py-2 border border-gray-300 rounded-lg hover:bg-gray-50 text-sm font-semibold"
              >
                Hủy
              </button>
              <button
                onClick={handleReject}
                disabled={actionLoading === 'reject'}
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
