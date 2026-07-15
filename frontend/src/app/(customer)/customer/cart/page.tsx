'use client';

import React from 'react';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { ShoppingBag, Trash2, Minus, Plus, AlertTriangle, ArrowRight, Loader2, RefreshCw } from 'lucide-react';
import { PageHeader } from '@/components/ui/page-header';
import { Button } from '@/components/ui/button';
import { Card, CardContent } from '@/components/ui/card';
import { showToast } from '@/components/ui/toast';
import { VoucherInput } from '@/components/cart/voucher-input';
import { useCart, useUpdateCartItem, useRemoveCartItem, useClearCart } from '@/features/cart/api';

export default function CustomerCartPage() {
  const router = useRouter();
  const { data: cart, isLoading, isError, refetch } = useCart();
  const updateMutation = useUpdateCartItem();
  const removeMutation = useRemoveCartItem();
  const clearMutation = useClearCart();

  const handleQuantityChange = (itemId: string, currentQty: number, change: number) => {
    const newQty = currentQty + change;
    if (newQty < 1) return;
    if (newQty > 99) {
      showToast.error('Số lượng tối đa cho mỗi món là 99');
      return;
    }
    updateMutation.mutate(
      { cartItemId: itemId, quantity: newQty },
      {
        onError: (err: any) => {
          const errMsg = err.response?.data?.message || 'Không thể cập nhật số lượng';
          showToast.error(errMsg);
        },
      }
    );
  };

  const handleRemoveItem = (itemId: string) => {
    removeMutation.mutate(itemId, {
      onSuccess: () => {
        showToast.success('Đã xóa món ăn khỏi giỏ hàng');
      },
      onError: () => {
        showToast.error('Không thể xóa món ăn');
      },
    });
  };

  const handleClearCart = () => {
    if (window.confirm('Bạn có chắc chắn muốn xóa toàn bộ giỏ hàng?')) {
      clearMutation.mutate(undefined, {
        onSuccess: () => {
          showToast.success('Đã xóa sạch giỏ hàng');
        },
        onError: () => {
          showToast.error('Không thể xóa giỏ hàng');
        },
      });
    }
  };

  // Loading State
  if (isLoading) {
    return (
      <div className="space-y-6">
        <PageHeader title="Giỏ hàng của tôi" description="Quản lý các món ăn bạn đã chọn." />
        <div className="flex flex-col items-center justify-center py-24 bg-white rounded-2xl border border-stone-200">
          <Loader2 size={36} className="text-primary-500 animate-spin mb-4" />
          <p className="text-stone-500 font-medium">Đang tải giỏ hàng của bạn...</p>
        </div>
      </div>
    );
  }

  // Error State
  if (isError) {
    return (
      <div className="space-y-6">
        <PageHeader title="Giỏ hàng của tôi" description="Quản lý các món ăn bạn đã chọn." />
        <div className="flex flex-col items-center justify-center py-20 bg-white rounded-2xl border border-stone-200 text-center px-4">
          <AlertTriangle size={40} className="text-red-500 mb-4" />
          <h3 className="text-lg font-bold text-stone-900 mb-2">Đã xảy ra lỗi</h3>
          <p className="text-stone-500 max-w-md mb-6">
            Không thể kết nối tới máy chủ để lấy thông tin giỏ hàng. Vui lòng thử lại.
          </p>
          <Button onClick={() => refetch()} className="gap-2">
            <RefreshCw size={16} /> Thử lại
          </Button>
        </div>
      </div>
    );
  }

  const hasItems = cart && cart.items.length > 0;

  // Empty State
  if (!cart || !hasItems) {
    return (
      <div className="space-y-6">
        <PageHeader title="Giỏ hàng của tôi" description="Quản lý các món ăn bạn đã chọn." />
        <div className="flex flex-col items-center justify-center py-20 bg-white rounded-2xl border border-stone-200 text-center px-4">
          <div className="w-16 h-16 bg-stone-100 rounded-full flex items-center justify-center mb-4">
            <ShoppingBag size={30} className="text-stone-400" />
          </div>
          <h3 className="text-lg font-bold text-stone-900 mb-1">Giỏ hàng của bạn đang trống</h3>
          <p className="text-stone-500 text-sm max-w-sm mb-6">
            Có vẻ như bạn chưa thêm món ăn nào vào giỏ hàng. Hãy khám phá thực đơn thơm ngon của chúng tôi ngay!
          </p>
          <Link href="/menu">
            <Button className="gap-2 bg-primary-500 hover:bg-primary-600">
              Xem thực đơn ngay <ArrowRight size={16} />
            </Button>
          </Link>
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <PageHeader title="Giỏ hàng của tôi" description={`Bạn có ${cart.itemCount} món ăn trong giỏ hàng.`} />
        <Button
          variant="ghost"
          onClick={handleClearCart}
          className="text-stone-500 hover:text-red-600 font-semibold gap-1.5 text-xs sm:text-sm"
          disabled={clearMutation.isPending}
        >
          <Trash2 size={16} /> Xóa sạch giỏ hàng
        </Button>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8 items-start">
        {/* Cart Items List */}
        <div className="lg:col-span-2 space-y-4">
          {cart.items.map((item) => (
            <Card
              key={item.id}
              className={`overflow-hidden border transition-all ${
                !item.isValid
                  ? 'border-red-200 bg-red-50/20'
                  : item.priceChanged
                  ? 'border-amber-200 bg-amber-50/20'
                  : 'border-stone-200 hover:border-stone-300'
              }`}
            >
              <CardContent className="p-4 sm:p-5">
                <div className="flex flex-col sm:flex-row gap-4 items-start sm:items-center justify-between">
                  {/* Product Details */}
                  <div className="flex gap-4 items-center">
                    {item.imageUrl ? (
                      <img
                        src={item.imageUrl}
                        alt={item.productName}
                        className="w-16 h-16 rounded-xl object-cover border border-stone-100 shrink-0"
                      />
                    ) : (
                      <div className="w-16 h-16 bg-stone-50 rounded-xl flex items-center justify-center text-3xl border border-stone-100 shrink-0 select-none">
                        🥣
                      </div>
                    )}
                    <div className="space-y-1">
                      <h4 className="font-bold text-stone-900 text-sm sm:text-base leading-snug">
                        {item.productName}
                      </h4>
                      {item.options.length > 0 && (
                        <p className="text-xs text-stone-500 font-medium">
                          Tùy chọn: {item.options.map((o) => o.optionName).join(', ')}
                        </p>
                      )}
                      {item.specialNote && (
                        <p className="text-xs text-stone-400 italic">
                          📝 Ghi chú: {item.specialNote}
                        </p>
                      )}

                      {/* Warnings */}
                      {item.priceChanged && (
                        <div className="flex items-center gap-1.5 text-xs text-amber-600 font-semibold bg-amber-50 border border-amber-100 rounded-lg px-2 py-0.5 w-fit mt-1">
                          <AlertTriangle size={12} />
                          Giá món ăn đã cập nhật
                        </div>
                      )}
                      {!item.isValid && (
                        <div className="flex items-center gap-1.5 text-xs text-red-600 font-semibold bg-red-50 border border-red-100 rounded-lg px-2 py-0.5 w-fit mt-1">
                          <AlertTriangle size={12} />
                          {item.validationErrors[0] || 'Cấu hình không khả dụng'}
                        </div>
                      )}
                    </div>
                  </div>

                  {/* Quantity and Line Total */}
                  <div className="flex items-center justify-between sm:justify-end gap-6 w-full sm:w-auto border-t sm:border-t-0 pt-3 sm:pt-0">
                    <div className="flex items-center gap-2 bg-stone-100 border border-stone-200 rounded-xl px-2 py-1">
                      <button
                        onClick={() => handleQuantityChange(item.id, item.quantity, -1)}
                        disabled={item.quantity <= 1 || updateMutation.isPending}
                        className="p-1 text-stone-500 hover:text-stone-900 disabled:opacity-30 transition-colors"
                      >
                        <Minus size={14} className="stroke-[2.5]" />
                      </button>
                      <span className="w-8 text-center text-sm font-extrabold text-stone-850">
                        {item.quantity}
                      </span>
                      <button
                        onClick={() => handleQuantityChange(item.id, item.quantity, 1)}
                        disabled={item.quantity >= 99 || updateMutation.isPending}
                        className="p-1 text-stone-500 hover:text-stone-900 disabled:opacity-30 transition-colors"
                      >
                        <Plus size={14} className="stroke-[2.5]" />
                      </button>
                    </div>

                    <div className="text-right">
                      <p className="text-[10px] text-stone-400 font-medium">Thành tiền</p>
                      <p className="font-extrabold text-primary-700 text-base sm:text-lg">
                        {item.lineTotal.toLocaleString('vi-VN')}đ
                      </p>
                    </div>

                    <button
                      onClick={() => handleRemoveItem(item.id)}
                      disabled={removeMutation.isPending}
                      className="p-2 text-stone-400 hover:text-red-600 hover:bg-red-50 rounded-xl transition-all"
                      title="Xóa món này"
                    >
                      <Trash2 size={18} />
                    </button>
                  </div>
                </div>
              </CardContent>
            </Card>
          ))}
        </div>

        {/* Sticky Summary Card */}
        <div className="space-y-6 lg:sticky lg:top-20">
          <Card className="border border-stone-200 shadow-sm bg-white overflow-hidden rounded-2xl">
            <CardContent className="p-5 sm:p-6 space-y-6">
              <h3 className="font-bold text-stone-900 text-base sm:text-lg border-b border-stone-100 pb-3">
                Tổng đơn hàng
              </h3>

              {/* Voucher Area */}
              <VoucherInput
                appliedVoucher={cart.appliedVoucher}
                voucherRemovalReason={cart.voucherRemovalReason}
                discountAmount={cart.discountAmount}
              />

              {/* Pricing Details */}
              <div className="space-y-3 text-sm border-t border-stone-100 pt-4">
                <div className="flex justify-between text-stone-500">
                  <span>Tạm tính</span>
                  <span className="font-bold text-stone-800">
                    {cart.subtotal.toLocaleString('vi-VN')}đ
                  </span>
                </div>

                {cart.discountAmount > 0 && (
                  <div className="flex justify-between text-green-600">
                    <span>Mã giảm giá áp dụng</span>
                    <span className="font-bold">
                      -{cart.discountAmount.toLocaleString('vi-VN')}đ
                    </span>
                  </div>
                )}

                <div className="flex justify-between text-stone-900 font-bold text-lg pt-3 border-t border-stone-100">
                  <span>Tổng cộng</span>
                  <span className="text-primary font-extrabold">
                    {cart.estimatedTotal.toLocaleString('vi-VN')}đ
                  </span>
                </div>
              </div>

              {/* Checkout CTA */}
              <div className="space-y-2">
                <Button
                  disabled
                  className="w-full py-6 font-bold text-sm bg-primary hover:bg-primary-dark transition-all rounded-xl cursor-not-allowed opacity-60 flex items-center justify-center gap-2"
                >
                  Tiến hành thanh toán
                </Button>
                <p className="text-[10px] text-center text-stone-400 font-medium italic">
                  * Chức năng thanh toán sẽ được hoàn thiện ở Giai đoạn 5.
                </p>
              </div>
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  );
}
