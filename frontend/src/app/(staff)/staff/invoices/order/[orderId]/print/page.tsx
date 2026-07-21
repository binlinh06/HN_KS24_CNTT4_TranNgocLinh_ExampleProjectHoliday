'use client';

import React, { useEffect, useState } from 'react';
import { useParams, useRouter } from 'next/navigation';
import { useInvoiceByOrder, markInvoicePrinted } from '@/features/staff/api';
import { toast } from 'react-hot-toast';

export default function InvoicePrintByOrderPage() {
  const params = useParams();
  const router = useRouter();
  const orderId = params.orderId as string;

  const { data: invoice, isLoading, error, refetch } = useInvoiceByOrder(orderId);
  const [printReason, setPrintReason] = useState<string>('');
  const [showReasonModal, setShowReasonModal] = useState<boolean>(false);
  const [isSubmitting, setIsSubmitting] = useState<boolean>(false);

  useEffect(() => {
    if (!invoice) return;

    // If it's a reprint (printCount > 0), we show a modal to input reprint reason
    if (invoice.printCount > 0) {
      setShowReasonModal(true);
    } else {
      // First print, record and print automatically
      recordFirstPrintAndPrint();
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [invoice]);

  const recordFirstPrintAndPrint = async () => {
    try {
      await markInvoicePrinted(invoice.id, 'In hóa đơn lần đầu');
      await refetch();
      setTimeout(() => {
        window.print();
      }, 500);
    } catch (e: any) {
      console.error('Lỗi ghi nhận in hóa đơn:', e);
      // Still trigger print anyway for customer service continuity
      window.print();
    }
  };

  const handleConfirmReprint = async () => {
    if (!printReason.trim()) {
      toast.error('Vui lòng nhập lý do in lại hóa đơn');
      return;
    }
    setIsSubmitting(true);
    try {
      await markInvoicePrinted(invoice.id, printReason.trim());
      await refetch();
      setShowReasonModal(false);
      toast.success('Đã ghi nhận sự kiện in lại');
      setTimeout(() => {
        window.print();
      }, 500);
    } catch (err: any) {
      toast.error(err.response?.data?.message || 'Lỗi khi lưu sự kiện in');
    } finally {
      setIsSubmitting(false);
    }
  };

  if (isLoading) {
    return (
      <div className="flex flex-col items-center justify-center min-h-screen bg-gray-50 text-gray-500 text-sm">
        <div className="animate-spin rounded-full h-8 w-8 border-t-2 border-b-2 border-emerald-600 mb-2"></div>
        Đang tải thông tin hóa đơn...
      </div>
    );
  }

  if (error || !invoice) {
    return (
      <div className="flex items-center justify-center min-h-screen bg-gray-50 text-red-600 text-sm p-4 text-center">
        Lỗi tải thông tin hóa đơn hoặc chưa xuất hóa đơn cho đơn hàng này.
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-white text-gray-900 p-4 font-mono text-xs flex flex-col items-center">
      {/* K80 invoice wrapper */}
      <div className="w-[80mm] max-w-full border-dashed border-gray-300 p-2 print:border-none print:p-0">
        <style jsx global>{`
          @media print {
            body {
              background: white;
              color: black;
            }
            .no-print {
              display: none !important;
            }
            @page {
              margin: 0;
              size: 80mm auto;
            }
          }
        `}</style>

        {/* Top Controls */}
        <div className="no-print mb-6 p-3 bg-gray-50 border border-gray-200 rounded-lg flex justify-between gap-2 w-full">
          <button 
            onClick={() => window.close()}
            className="px-3 py-1.5 bg-gray-200 hover:bg-gray-300 rounded font-semibold text-gray-800"
          >
            Đóng trang
          </button>
          <button 
            onClick={() => {
              if (invoice.printCount > 0) {
                setShowReasonModal(true);
              } else {
                window.print();
              }
            }}
            className="px-3 py-1.5 bg-emerald-600 hover:bg-emerald-700 text-white rounded font-semibold"
          >
            In lại hóa đơn
          </button>
        </div>

        {/* Brand header */}
        <div className="text-center space-y-1 mb-4">
          <h2 className="text-sm font-bold uppercase tracking-wider">PHỞ BÒ GIA TRUYỀN</h2>
          <p className="text-[10px] text-gray-500">Đ/C: 123 Phố Phở, Hà Nội</p>
          <p className="text-[10px] text-gray-500">Hotline: 0987.654.321</p>
          <div className="border-b border-dashed border-gray-400 my-2"></div>
          <h3 className="text-xs font-bold">HÓA ĐƠN THANH TOÁN</h3>
          <p className="text-[9px] text-gray-400">Số: {invoice.invoiceNumber}</p>
        </div>

        {/* Invoice Metadata */}
        <div className="space-y-1 mb-4 text-[10px]">
          <div>Mã đơn: <span className="font-bold">{invoice.orderCode}</span></div>
          {invoice.tableNumber && <div>Bàn số: <span className="font-bold">{invoice.tableNumber}</span></div>}
          <div>Hình thức: <span>{invoice.orderType}</span></div>
          <div>Thu ngân: <span>{invoice.issuedByName}</span></div>
          <div>Thời gian: <span>{new Date(invoice.issuedAt).toLocaleString('vi-VN')}</span></div>
          <div>In lần: <span>{invoice.printCount}</span></div>
          {invoice.lastPrintedAt && (
            <div className="text-[9px] text-gray-500">
              Lần in cuối: {new Date(invoice.lastPrintedAt).toLocaleString('vi-VN')}
            </div>
          )}
          <div className="border-b border-dashed border-gray-400 my-2"></div>
        </div>

        {/* Items list */}
        <table className="w-full text-left text-[10px] border-collapse mb-4">
          <thead>
            <tr className="border-b border-gray-300 font-bold">
              <th className="py-1">Món ăn</th>
              <th className="py-1 text-center">SL</th>
              <th className="py-1 text-right">Thành tiền</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-dashed divide-gray-200">
            {invoice.items && invoice.items.map((item: any, idx: number) => (
              <tr key={idx} className="align-top">
                <td className="py-1.5 pr-2">
                  <div className="font-bold">{item.productName}</div>
                  {item.options && item.options.length > 0 && (
                    <div className="text-[9px] text-gray-500 italic pl-2">
                      + {item.options.join(', ')}
                    </div>
                  )}
                </td>
                <td className="py-1.5 text-center">{item.quantity}</td>
                <td className="py-1.5 text-right font-bold">
                  {item.total.toLocaleString('vi-VN')}đ
                </td>
              </tr>
            ))}
          </tbody>
        </table>

        {/* Invoice Summary */}
        <div className="border-t border-dashed border-gray-400 pt-2 space-y-1.5 text-[10px]">
          <div className="flex justify-between">
            <span>Tạm tính:</span>
            <span>{invoice.subtotal.toLocaleString('vi-VN')}đ</span>
          </div>
          <div className="flex justify-between">
            <span>Giảm giá:</span>
            <span>-{invoice.discountAmount.toLocaleString('vi-VN')}đ</span>
          </div>
          <div className="flex justify-between font-bold text-sm border-t border-dashed border-gray-200 pt-1.5">
            <span>TỔNG CỘNG:</span>
            <span>{invoice.finalAmount.toLocaleString('vi-VN')}đ</span>
          </div>
          <div className="flex justify-between text-[9px] text-gray-500">
            <span>Thanh toán:</span>
            <span>{invoice.paymentMethod}</span>
          </div>
        </div>

        {/* Receipt Footer */}
        <div className="text-center space-y-1 mt-6 text-[10px] border-t border-dashed border-gray-400 pt-4">
          <p className="font-bold italic">Xin cảm ơn quý khách!</p>
          <p className="text-[9px] text-gray-400">Hẹn gặp lại quý khách lần sau!</p>
        </div>
      </div>

      {/* Reprint Reason Modal */}
      {showReasonModal && (
        <div className="no-print fixed inset-0 bg-black bg-opacity-40 flex items-center justify-center p-4 z-50">
          <div className="bg-white rounded-xl border border-gray-200 shadow-lg max-w-sm w-full p-6 space-y-4 font-sans text-sm">
            <h3 className="text-lg font-bold text-gray-900">In lại hóa đơn</h3>
            <p className="text-xs text-gray-500">
              Đây là bản in lại của hóa đơn số <span className="font-bold">{invoice.invoiceNumber}</span>. Vui lòng ghi nhận lý do in lại để phục vụ đối soát.
            </p>
            
            <div className="space-y-1">
              <label className="text-xs font-semibold text-gray-600 uppercase">Lý do in lại:</label>
              <textarea
                value={printReason}
                onChange={(e) => setPrintReason(e.target.value)}
                placeholder="Ví dụ: Khách yêu cầu, kẹt giấy máy in..."
                rows={3}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-1 focus:ring-amber-500"
              />
            </div>

            <div className="flex justify-end gap-2 pt-2">
              <button
                onClick={() => {
                  setShowReasonModal(false);
                  router.back();
                }}
                className="px-3 py-1.5 border border-gray-300 rounded hover:bg-gray-50 text-xs font-semibold"
              >
                Hủy / Trở lại
              </button>
              <button
                onClick={handleConfirmReprint}
                disabled={isSubmitting}
                className="px-4 py-1.5 bg-amber-600 hover:bg-amber-700 text-white rounded text-xs font-semibold shadow-sm disabled:opacity-50"
              >
                Ghi nhận & In
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
