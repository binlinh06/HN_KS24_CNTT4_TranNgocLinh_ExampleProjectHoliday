'use client';

import React, { useState, Suspense } from 'react';
import { useSearchParams, useRouter } from 'next/navigation';
import { useOrderDetails, usePaymentDetails, useRetryPayment } from '@/features/checkout/api';
import { v4 as uuidv4 } from 'uuid';

function CheckoutResultComponent() {
  const searchParams = useSearchParams();
  const router = useRouter();

  const orderId = searchParams.get('orderId') || '';

  const { data: order, isLoading: orderLoading } = useOrderDetails(orderId);
  const { data: payment, isLoading: paymentLoading } = usePaymentDetails(orderId, !!orderId && !!order?.paymentMethod);
  const retryMutation = useRetryPayment();

  const [isProcessing, setIsProcessing] = useState(false);
  const [error, setError] = useState('');

  const handleRetryPayment = async () => {
    setIsProcessing(true);
    setError('');

    try {
      const idempotencyKey = uuidv4();
      const paymentRes = await retryMutation.mutateAsync({
        orderId,
        idempotencyKey,
      });

      if (paymentRes.paymentUrl) {
        window.location.href = paymentRes.paymentUrl;
      } else {
        setError('Không thể lấy liên kết thanh toán trực tuyến mới.');
        setIsProcessing(false);
      }
    } catch (err: any) {
      setIsProcessing(false);
      setError(err.response?.data?.message || 'Khởi tạo lại giao dịch thanh toán thất bại.');
    }
  };

  if (!orderId) {
    return (
      <div className="max-w-md mx-auto px-4 py-12">
        <div className="bg-white border border-gray-200 rounded-2xl shadow-sm p-6 text-center">
          <p className="text-sm text-gray-500">Không tìm thấy thông tin đơn hàng.</p>
          <button
            onClick={() => router.push('/menu')}
            className="mt-4 px-4 py-2 bg-[#0F6B4F] text-white rounded-lg text-sm font-semibold"
          >
            Quay lại thực đơn
          </button>
        </div>
      </div>
    );
  }

  if (orderLoading || paymentLoading) {
    return (
      <div className="flex justify-center items-center py-20">
        <div className="animate-spin rounded-full h-10 w-10 border-b-2 border-[#0F6B4F]"></div>
      </div>
    );
  }

  // Determine display state from backend data, NOT from URL query params
  const isCod = order?.paymentMethod === 'COD';
  const isOnline = order?.paymentMethod === 'ONLINE_GATEWAY';
  const paymentStatus = payment?.paymentStatus || order?.paymentStatus;

  const isPaymentSuccess = paymentStatus === 'SUCCESS';
  const isPaymentPending = paymentStatus === 'PENDING';
  const isPaymentFailed = paymentStatus === 'FAILED' || paymentStatus === 'CANCELED' || paymentStatus === 'EXPIRED';

  // COD always shows order success
  const showSuccess = isCod || (isOnline && isPaymentSuccess);
  const showPending = isOnline && isPaymentPending;
  const showFailed = isOnline && isPaymentFailed;

  return (
    <div className="max-w-md mx-auto px-4 py-12 space-y-6">
      {/* Result Card */}
      <div className="bg-white border border-gray-200 rounded-2xl shadow-sm p-6 text-center space-y-6">
        {showSuccess && (
          <>
            {/* Success checkmark */}
            <div className="w-16 h-16 bg-emerald-50 text-[#0F6B4F] rounded-full flex items-center justify-center mx-auto border border-emerald-100">
              <svg xmlns="http://www.w3.org/2000/svg" className="h-8 w-8" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
              </svg>
            </div>
            <div className="space-y-1.5">
              {isCod ? (
                <>
                  <h2 className="text-xl font-bold text-gray-900">Đặt hàng thành công!</h2>
                  <p className="text-sm text-gray-500">Thanh toán khi nhận hàng. Cảm ơn bạn đã lựa chọn Phở Bò Gia Truyền.</p>
                </>
              ) : (
                <>
                  <h2 className="text-xl font-bold text-gray-900">Thanh toán thành công!</h2>
                  <p className="text-sm text-gray-500">Giao dịch đã được xác nhận. Cảm ơn bạn đã lựa chọn Phở Bò Gia Truyền.</p>
                </>
              )}
            </div>
          </>
        )}

        {showPending && (
          <>
            {/* Pending clock */}
            <div className="w-16 h-16 bg-amber-50 text-amber-600 rounded-full flex items-center justify-center mx-auto border border-amber-100">
              <svg xmlns="http://www.w3.org/2000/svg" className="h-8 w-8" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
            </div>
            <div className="space-y-1.5">
              <h2 className="text-xl font-bold text-gray-900">Thanh toán đang xử lý</h2>
              <p className="text-sm text-gray-500">
                Giao dịch đang chờ xác nhận từ cổng thanh toán. Vui lòng đợi hoặc kiểm tra lại sau.
              </p>
            </div>
          </>
        )}

        {showFailed && (
          <>
            {/* Failure cross */}
            <div className="w-16 h-16 bg-red-50 text-red-600 rounded-full flex items-center justify-center mx-auto border border-red-100">
              <svg xmlns="http://www.w3.org/2000/svg" className="h-8 w-8" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
              </svg>
            </div>
            <div className="space-y-1.5">
              <h2 className="text-xl font-bold text-gray-900">Thanh toán chưa thành công</h2>
              <p className="text-sm text-gray-500">
                Giao dịch thanh toán trực tuyến bị gián đoạn hoặc thất bại.
              </p>
            </div>
          </>
        )}

        {/* Order Details box */}
        {order && (
          <div className="bg-gray-50 border border-gray-150 rounded-xl p-4 text-xs text-left space-y-2.5">
            <div className="flex justify-between">
              <span className="text-gray-500">Mã đơn hàng:</span>
              <span className="font-semibold text-gray-800">{order.orderCode}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-gray-500">Hình thức thanh toán:</span>
              <span className="font-medium text-gray-700">
                {order.paymentMethod === 'ONLINE_GATEWAY' ? 'Thanh toán trực tuyến VNPay' : 'Thanh toán khi nhận hàng'}
              </span>
            </div>
            <div className="flex justify-between">
              <span className="text-gray-500">Trạng thái thanh toán:</span>
              <span
                className={`font-semibold ${
                  isPaymentSuccess ? 'text-emerald-600' :
                  isPaymentPending ? 'text-amber-600' :
                  'text-red-600'
                }`}
              >
                {isPaymentSuccess ? 'Đã thanh toán' :
                 isPaymentPending ? 'Đang xử lý' :
                 isCod ? 'Thanh toán khi nhận hàng' :
                 'Chưa thanh toán'}
              </span>
            </div>
            <div className="flex justify-between border-t border-gray-200 pt-2.5">
              <span className="text-gray-500 font-medium">Tổng số tiền:</span>
              <span className="font-bold text-sm text-[#0F6B4F]">
                {order.finalAmount.toLocaleString('vi-VN')}đ
              </span>
            </div>
          </div>
        )}

        {error && (
          <div className="bg-red-50 border border-red-200 text-red-700 p-3 rounded-lg text-xs">
            {error}
          </div>
        )}

        {/* Action buttons */}
        <div className="space-y-3 pt-2">
          {showFailed && order && (
            <button
              onClick={handleRetryPayment}
              disabled={isProcessing}
              className="w-full py-2.5 bg-[#0F6B4F] hover:bg-[#0c5942] disabled:opacity-50 text-white font-bold rounded-lg text-sm shadow transition-colors flex items-center justify-center gap-1.5"
            >
              {isProcessing ? (
                <>
                  <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-white"></div>
                  Đang khởi tạo lại...
                </>
              ) : (
                'Thử lại thanh toán trực tuyến'
              )}
            </button>
          )}

          <button
            onClick={() => router.push('/customer/orders')}
            className="w-full py-2.5 bg-white border border-gray-300 hover:bg-gray-50 text-gray-700 font-semibold rounded-lg text-sm transition-colors"
          >
            Lịch sử mua hàng
          </button>

          <button
            onClick={() => router.push('/menu')}
            className="w-full py-2 bg-transparent text-[#0F6B4F] hover:underline text-xs font-semibold"
          >
            Quay lại thực đơn chính
          </button>
        </div>
      </div>
    </div>
  );
}

export default function CheckoutResultPage() {
  return (
    <Suspense fallback={<div className="flex justify-center items-center py-20"><div className="animate-spin rounded-full h-10 w-10 border-b-2 border-[#0F6B4F]"></div></div>}>
      <CheckoutResultComponent />
    </Suspense>
  );
}
