'use client';

import React, { useState, Suspense } from 'react';
import { useSearchParams, useRouter } from 'next/navigation';
import { useSimulateMockPayment } from '@/features/checkout/api';

type SimulationStatus = 'SUCCESS' | 'FAILED' | 'CANCELED' | 'EXPIRED';

function PaymentMockComponent() {
  const searchParams = useSearchParams();
  const router = useRouter();

  const paymentId = searchParams.get('paymentId') || '';
  const amount = searchParams.get('amount') || '';
  const orderId = searchParams.get('orderId') || '';

  const simulateMutation = useSimulateMockPayment();

  const [error, setError] = useState('');

  if (!paymentId || !orderId) {
    return (
      <div className="min-h-screen bg-slate-900 flex flex-col justify-center items-center px-4 py-12 text-white font-sans">
        <div className="max-w-md w-full bg-slate-800 border border-slate-700 rounded-2xl shadow-2xl p-6">
          <div className="bg-red-950/40 border border-red-800 text-red-300 p-4 rounded-xl text-xs text-center">
            Tham số thanh toán không hợp lệ hoặc bị thiếu.
          </div>
        </div>
      </div>
    );
  }

  const handleSimulatePayment = async (status: SimulationStatus) => {
    setError('');

    try {
      await simulateMutation.mutateAsync({ paymentId, status });
      router.push(`/customer/checkout/result?orderId=${orderId}`);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Có lỗi xảy ra khi mô phỏng thanh toán.');
    }
  };

  const isProcessing = simulateMutation.isPending;

  return (
    <div className="min-h-screen bg-slate-900 flex flex-col justify-center items-center px-4 py-12 text-white font-sans">
      <div className="max-w-md w-full bg-slate-800 border border-slate-700 rounded-2xl shadow-2xl p-6 space-y-6">
        {/* Header */}
        <div className="text-center space-y-2">
          <div className="inline-flex items-center justify-center w-12 h-12 bg-amber-500/10 text-amber-500 rounded-full mb-2">
            <svg xmlns="http://www.w3.org/2000/svg" className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z" />
            </svg>
          </div>
          <h2 className="text-xl font-bold tracking-tight">CỔNG THANH TOÁN MOCK VNPAY</h2>
          <p className="text-xs text-slate-400">Chỉ sử dụng cho môi trường kiểm thử (Development/Sandbox)</p>
        </div>

        {error && (
          <div className="bg-red-950/40 border border-red-800 text-red-300 p-4 rounded-xl text-xs text-center">
            {error}
          </div>
        )}

        {/* Info Summary */}
        <div className="bg-slate-900/60 border border-slate-700/50 rounded-xl p-4 space-y-3 text-sm">
          <div className="flex justify-between border-b border-slate-800 pb-2">
            <span className="text-slate-400">Mã đơn hàng:</span>
            <span className="font-semibold text-slate-200">{orderId.substring(0, 8)}...</span>
          </div>
          <div className="flex justify-between border-b border-slate-800 pb-2">
            <span className="text-slate-400">Mã giao dịch:</span>
            <span className="font-mono text-slate-200 text-xs">{paymentId}</span>
          </div>
          {amount && (
            <div className="flex justify-between pt-1">
              <span className="text-slate-400">Số tiền thanh toán:</span>
              <span className="font-bold text-amber-400 text-base">
                {parseFloat(amount).toLocaleString('vi-VN')}đ
              </span>
            </div>
          )}
        </div>

        {/* Action Buttons */}
        <div className="space-y-3">
          <button
            onClick={() => handleSimulatePayment('SUCCESS')}
            disabled={isProcessing}
            className="w-full py-3 bg-[#0F6B4F] hover:bg-[#0c5942] disabled:opacity-50 text-white font-bold rounded-xl text-sm transition-colors shadow-lg shadow-green-950/20"
          >
            {isProcessing ? 'Đang gửi kết quả...' : 'Thanh toán THÀNH CÔNG (SUCCESS)'}
          </button>

          <button
            onClick={() => handleSimulatePayment('FAILED')}
            disabled={isProcessing}
            className="w-full py-3 bg-red-600 hover:bg-red-700 disabled:opacity-50 text-white font-bold rounded-xl text-sm transition-colors"
          >
            {isProcessing ? 'Đang gửi kết quả...' : 'Thanh toán THẤT BẠI (FAILED)'}
          </button>

          <button
            onClick={() => handleSimulatePayment('CANCELED')}
            disabled={isProcessing}
            className="w-full py-3 bg-amber-600 hover:bg-amber-700 disabled:opacity-50 text-white font-bold rounded-xl text-sm transition-colors"
          >
            {isProcessing ? 'Đang gửi kết quả...' : 'Hủy giao dịch (CANCELED)'}
          </button>

          <button
            onClick={() => handleSimulatePayment('EXPIRED')}
            disabled={isProcessing}
            className="w-full py-3 bg-slate-600 hover:bg-slate-700 disabled:opacity-50 text-white font-bold rounded-xl text-sm transition-colors"
          >
            {isProcessing ? 'Đang gửi kết quả...' : 'Hết hạn giao dịch (EXPIRED)'}
          </button>
        </div>

        <div className="text-[10px] text-center text-slate-500">
          * Kết quả thanh toán được xử lý hoàn toàn trên server. Không có secret nào được lưu trên trình duyệt.
        </div>
      </div>
    </div>
  );
}

export default function PaymentMockPage() {
  return (
    <Suspense fallback={<div className="min-h-screen bg-slate-900 flex items-center justify-center text-white">Đang tải cấu hình thanh toán...</div>}>
      <PaymentMockComponent />
    </Suspense>
  );
}
