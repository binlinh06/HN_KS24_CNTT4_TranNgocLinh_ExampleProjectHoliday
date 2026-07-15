'use client';

import React, { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { PageHeader } from '@/components/ui/page-header';
import { useCart } from '@/features/cart/api';
import { useAddresses } from '@/features/addresses/api';
import { usePreviewCheckout, useCheckoutOrder, useInitiatePayment } from '@/features/checkout/api';
import { CheckoutPreviewRequest } from '@/types';
import { v4 as uuidv4 } from 'uuid';

export default function CustomerCheckoutPage() {
  const router = useRouter();
  const { data: cart, isLoading: isCartLoading } = useCart();
  const { data: addresses, isLoading: isAddressesLoading } = useAddresses();

  const checkoutMutation = useCheckoutOrder();
  const initiatePaymentMutation = useInitiatePayment();

  // Selection states
  const [selectedAddressId, setSelectedAddressId] = useState<string>('');
  const [paymentMethod, setPaymentMethod] = useState<'COD' | 'ONLINE'>('COD');
  const [customerNote, setCustomerNote] = useState('');
  const [checkoutIdempotencyKey, setCheckoutIdempotencyKey] = useState<string>('');

  // Local submit status
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');

  // Generate Idempotency Key once per checkout page load to protect against double submit & network retries
  useEffect(() => {
    setCheckoutIdempotencyKey(uuidv4());
  }, []);

  // Pre-select default address
  useEffect(() => {
    if (addresses && addresses.length > 0) {
      const defaultAddr = addresses.find((a) => a.isDefault);
      if (defaultAddr) {
        setSelectedAddressId(defaultAddr.id);
      } else {
        setSelectedAddressId(addresses[0].id);
      }
    }
  }, [addresses]);

  // Preview request object
  const previewReq: CheckoutPreviewRequest = {
    addressId: selectedAddressId,
    paymentMethod,
    customerNote,
  };

  const { data: preview, isLoading: isPreviewLoading } = usePreviewCheckout(
    previewReq,
    !!selectedAddressId
  );

  const handlePlaceOrder = async () => {
    if (!selectedAddressId) {
      setErrorMessage('Vui lòng chọn địa chỉ giao hàng.');
      return;
    }

    setIsSubmitting(true);
    setErrorMessage('');

    try {
      // 1. Submit Order
      const order = await checkoutMutation.mutateAsync({
        data: {
          addressId: selectedAddressId,
          paymentMethod,
          customerNote,
        },
        idempotencyKey: checkoutIdempotencyKey,
      });

      // 2. Handle payment method
      if (paymentMethod === 'ONLINE') {
        // Initiate ONLINE payment to get URL
        const initiateIdempotency = uuidv4();
        const paymentRes = await initiatePaymentMutation.mutateAsync({
          orderId: order.orderId,
          idempotencyKey: initiateIdempotency,
        });

        if (paymentRes.paymentUrl) {
          // Redirect browser to VNPay/Mock Gateway url
          window.location.href = paymentRes.paymentUrl;
        } else {
          router.push(`/customer/checkout/result?orderId=${order.orderId}&status=error&message=NoPaymentUrl`);
        }
      } else {
        // COD Success
        router.push(`/customer/checkout/result?orderId=${order.orderId}&status=success`);
      }
    } catch (err: any) {
      setIsSubmitting(false);
      // Generate new idempotency key for subsequent retries if needed
      setCheckoutIdempotencyKey(uuidv4());
      setErrorMessage(
        err.response?.data?.message || 'Đặt hàng thất bại. Vui lòng kiểm tra lại cấu hình món ăn hoặc voucher.'
      );
    }
  };

  if (isCartLoading || isAddressesLoading) {
    return (
      <div className="flex justify-center items-center py-20">
        <div className="animate-spin rounded-full h-10 w-10 border-b-2 border-[#0F6B4F]"></div>
      </div>
    );
  }

  const hasItems = cart && cart.items && cart.items.length > 0;

  if (!hasItems) {
    return (
      <div className="max-w-2xl mx-auto text-center py-16 px-4">
        <div className="w-16 h-16 bg-gray-50 text-gray-400 rounded-full flex items-center justify-center mx-auto mb-4">
          <svg xmlns="http://www.w3.org/2000/svg" className="h-8 w-8" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M16 11V7a4 4 0 00-8 0v4M5 9h14l1 12H4L5 9z" />
          </svg>
        </div>
        <h3 className="text-xl font-bold text-gray-800 mb-2">Giỏ hàng của bạn đang trống</h3>
        <p className="text-gray-500 mb-6">Thêm các món phở thơm ngon và thức uống thanh mát trước khi thanh toán.</p>
        <button
          onClick={() => router.push('/menu')}
          className="px-6 py-2.5 bg-[#0F6B4F] hover:bg-[#0c5942] text-white font-medium rounded-lg transition-colors shadow-sm"
        >
          Xem thực đơn phở
        </button>
      </div>
    );
  }

  return (
    <div className="space-y-6 max-w-5xl mx-auto px-4 py-6">
      <PageHeader
        title="Thanh toán & Đặt hàng"
        description="Kiểm tra lại thực đơn phở, chọn địa chỉ nhận hàng và hình thức thanh toán."
      />

      {errorMessage && (
        <div className="bg-red-50 border border-red-200 text-red-700 p-4 rounded-lg text-sm shadow-sm flex items-start gap-2.5">
          <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 text-red-500 shrink-0" viewBox="0 0 20 20" fill="currentColor">
            <path fillRule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7 4a1 1 0 11-2 0 1 1 0 012 0zm-1-9a1 1 0 00-1 1v4a1 1 0 102 0V6a1 1 0 00-1-1z" clipRule="evenodd" />
          </svg>
          <div>{errorMessage}</div>
        </div>
      )}

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Left Side: Forms */}
        <div className="lg:col-span-2 space-y-6">
          {/* 1. Address Selection */}
          <div className="bg-white border border-gray-200 rounded-xl p-5 shadow-sm space-y-4">
            <h3 className="text-md font-bold text-gray-900 flex items-center gap-2">
              <span className="w-5 h-5 bg-[#0F6B4F] text-white rounded-full text-xs flex items-center justify-center">1</span>
              Địa chỉ nhận hàng
            </h3>

            {addresses && addresses.length === 0 ? (
              <div className="text-center py-6 border border-dashed border-gray-250 rounded-lg">
                <p className="text-gray-500 text-sm mb-3">Bạn chưa cấu hình địa chỉ giao hàng nào.</p>
                <button
                  onClick={() => router.push('/customer/addresses')}
                  className="px-4 py-1.5 border border-[#0F6B4F] text-[#0F6B4F] hover:bg-green-50 font-medium rounded-lg text-sm transition-colors"
                >
                  Thêm địa chỉ giao hàng
                </button>
              </div>
            ) : (
              <div className="space-y-3">
                <select
                  value={selectedAddressId}
                  onChange={(e) => setSelectedAddressId(e.target.value)}
                  className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-1 focus:ring-[#0F6B4F] focus:border-[#0F6B4F]"
                >
                  <option value="" disabled>-- Chọn địa chỉ nhận hàng --</option>
                  {addresses?.map((addr) => (
                    <option key={addr.id} value={addr.id}>
                      {addr.receiverName} ({addr.receiverPhone}) - {addr.addressDetail} {addr.isDefault ? '[Mặc định]' : ''}
                    </option>
                  ))}
                </select>

                {selectedAddressId && (
                  <div className="p-3 bg-gray-50 border border-gray-150 rounded-lg text-xs text-gray-600 flex justify-between items-center">
                    <span>
                      Địa chỉ đang chọn sẽ được lưu snapshot vào đơn hàng để bảo vệ dữ liệu khi thông tin sổ địa chỉ thay đổi sau này.
                    </span>
                    <button
                      onClick={() => router.push('/customer/addresses')}
                      className="text-[#0F6B4F] font-semibold hover:underline shrink-0 ml-2"
                    >
                      Quản lý
                    </button>
                  </div>
                )}
              </div>
            )}
          </div>

          {/* 2. Payment Method */}
          <div className="bg-white border border-gray-200 rounded-xl p-5 shadow-sm space-y-4">
            <h3 className="text-md font-bold text-gray-900 flex items-center gap-2">
              <span className="w-5 h-5 bg-[#0F6B4F] text-white rounded-full text-xs flex items-center justify-center">2</span>
              Hình thức thanh toán
            </h3>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
              <label
                className={`p-4 border rounded-xl cursor-pointer flex items-start gap-3 transition-all ${
                  paymentMethod === 'COD'
                    ? 'border-[#0F6B4F] bg-green-50/40 ring-1 ring-[#0F6B4F]'
                    : 'border-gray-200 hover:bg-gray-50'
                }`}
              >
                <input
                  type="radio"
                  name="payment"
                  value="COD"
                  checked={paymentMethod === 'COD'}
                  onChange={() => setPaymentMethod('COD')}
                  className="mt-1 h-4 w-4 text-[#0F6B4F] focus:ring-[#0F6B4F]"
                />
                <div>
                  <div className="font-bold text-sm text-gray-900">Thanh toán khi nhận hàng (COD)</div>
                  <div className="text-xs text-gray-500 mt-0.5">Trả tiền mặt hoặc chuyển khoản khi phở giao tới nơi.</div>
                </div>
              </label>

              <label
                className={`p-4 border rounded-xl cursor-pointer flex items-start gap-3 transition-all ${
                  paymentMethod === 'ONLINE'
                    ? 'border-[#0F6B4F] bg-green-50/40 ring-1 ring-[#0F6B4F]'
                    : 'border-gray-200 hover:bg-gray-50'
                }`}
              >
                <input
                  type="radio"
                  name="payment"
                  value="ONLINE"
                  checked={paymentMethod === 'ONLINE'}
                  onChange={() => setPaymentMethod('ONLINE')}
                  className="mt-1 h-4 w-4 text-[#0F6B4F] focus:ring-[#0F6B4F]"
                />
                <div>
                  <div className="font-bold text-sm text-gray-900">Thanh toán trực tuyến (ONLINE)</div>
                  <div className="text-xs text-gray-500 mt-0.5">Thanh toán qua cổng VNPay (Mock sandbox cho môi trường dev).</div>
                </div>
              </label>
            </div>
          </div>

          {/* 3. Note */}
          <div className="bg-white border border-gray-200 rounded-xl p-5 shadow-sm space-y-3">
            <h3 className="text-md font-bold text-gray-900 flex items-center gap-2">
              <span className="w-5 h-5 bg-[#0F6B4F] text-white rounded-full text-xs flex items-center justify-center">3</span>
              Ghi chú đơn hàng (Không bắt buộc)
            </h3>
            <textarea
              value={customerNote}
              onChange={(e) => setCustomerNote(e.target.value)}
              placeholder="Nhập ghi chú cho quán (ví dụ: Không hành, nhiều giá, xin thêm thìa đũa...)"
              rows={2}
              maxLength={150}
              className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-1 focus:ring-[#0F6B4F] focus:border-[#0F6B4F]"
            />
          </div>
        </div>

        {/* Right Side: Order Summary */}
        <div className="space-y-6">
          <div className="bg-white border border-gray-200 rounded-xl p-5 shadow-sm space-y-4 sticky top-6">
            <h3 className="text-md font-bold text-gray-900 pb-3 border-b border-gray-150">Đơn hàng của bạn</h3>

            {/* Cart Items List */}
            <div className="max-h-48 overflow-y-auto space-y-3 pr-1">
              {cart?.items.map((item) => (
                <div key={item.id} className="flex justify-between items-start text-xs gap-3">
                  <div className="space-y-0.5 flex-1">
                    <div className="font-semibold text-gray-800">
                      {item.productName} <span className="text-gray-500">x{item.quantity}</span>
                    </div>
                    {item.options && item.options.length > 0 && (
                      <div className="text-[10px] text-gray-400">
                        {item.options.map((o) => o.optionName).join(', ')}
                      </div>
                    )}
                  </div>
                  <span className="font-medium text-gray-700 shrink-0">
                    {(item.currentUnitPrice * item.quantity).toLocaleString('vi-VN')}đ
                  </span>
                </div>
              ))}
            </div>

            {/* Summary calculations */}
            <div className="pt-3 border-t border-gray-150 space-y-2 text-xs">
              <div className="flex justify-between text-gray-600">
                <span>Tạm tính món ăn:</span>
                <span>{preview?.subtotal.toLocaleString('vi-VN') || cart?.subtotal.toLocaleString('vi-VN')}đ</span>
              </div>
              {preview?.appliedVoucherCode && (
                <div className="flex justify-between text-emerald-600 font-medium">
                  <span>Mã giảm giá ({preview.appliedVoucherCode}):</span>
                  <span>-{preview.discountAmount.toLocaleString('vi-VN')}đ</span>
                </div>
              )}
              <div className="flex justify-between text-gray-600">
                <span>Phí vận chuyển:</span>
                <span>0đ</span>
              </div>

              {preview?.warnings && preview.warnings.length > 0 && (
                <div className="bg-orange-50 border border-orange-200 text-orange-800 p-2.5 rounded text-[10px] space-y-1">
                  <div className="font-semibold uppercase tracking-wider text-[9px]">Lưu ý từ hệ thống:</div>
                  {preview.warnings.map((w, i) => (
                    <div key={i}>• {w}</div>
                  ))}
                </div>
              )}

              <div className="flex justify-between text-base font-bold text-gray-900 pt-2 border-t border-gray-150">
                <span>Tổng cộng:</span>
                <span className="text-[#0F6B4F]">
                  {(preview?.finalAmount ?? cart?.estimatedTotal).toLocaleString('vi-VN')}đ
                </span>
              </div>
            </div>

            {/* Idempotent Place Order Button */}
            <button
              onClick={handlePlaceOrder}
              disabled={isSubmitting || !selectedAddressId || (preview !== undefined && !preview.valid)}
              className="w-full py-3 bg-[#0F6B4F] hover:bg-[#0c5942] disabled:bg-gray-300 disabled:cursor-not-allowed text-white font-bold rounded-lg text-sm shadow transition-colors flex items-center justify-center gap-2"
            >
              {isSubmitting ? (
                <>
                  <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-white"></div>
                  Đang xử lý đơn hàng...
                </>
              ) : (
                'Đặt hàng ngay'
              )}
            </button>

            {preview && !preview.valid && (
              <p className="text-[10px] text-red-500 text-center">
                Giỏ hàng hoặc các điều kiện đặt hàng không hợp lệ. Vui lòng kiểm tra lại.
              </p>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
