'use client';

import React, { useEffect } from 'react';
import Link from 'next/link';
import Image from 'next/image';
import { X, ShoppingBag, Trash2, Minus, Plus, AlertTriangle, ArrowRight } from 'lucide-react';
import { useAuthStore } from '@/stores/auth';
import { useGuestCartStore } from '@/stores/cart';
import { useCart, useUpdateCartItem, useRemoveCartItem } from '@/features/cart/api';
import { Button } from '@/components/ui/button';
import { showToast } from '@/components/ui/toast';
import { CartResponse, CartItemResponse, GuestCartItem } from '@/types';

interface CartDrawerProps {
  isOpen: boolean;
  onClose: () => void;
}

export function CartDrawer({ isOpen, onClose }: CartDrawerProps) {
  const { isAuthenticated } = useAuthStore();
  const guestItems = useGuestCartStore((s) => s.items);
  const guestUpdateQty = useGuestCartStore((s) => s.updateQuantity);
  const guestRemove = useGuestCartStore((s) => s.removeItem);

  const { data: cart, isLoading } = useCart(isAuthenticated);
  const updateMutation = useUpdateCartItem();
  const removeMutation = useRemoveCartItem();

  // 1. Lock Body Scroll & ESC Key Listener
  useEffect(() => {
    if (!isOpen) return;

    // Lock body scroll
    const originalStyle = window.getComputedStyle(document.body).overflow;
    document.body.style.overflow = 'hidden';

    // Close on Escape key press
    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.key === 'Escape') {
        onClose();
      }
    };

    window.addEventListener('keydown', handleKeyDown);

    return () => {
      document.body.style.overflow = originalStyle;
      window.removeEventListener('keydown', handleKeyDown);
    };
  }, [isOpen, onClose]);

  const handleAuthQtyChange = (itemId: string, newQty: number) => {
    if (newQty < 1) return;
    if (newQty > 99) {
      showToast.error('Số lượng tối đa là 99');
      return;
    }
    updateMutation.mutate({ cartItemId: itemId, quantity: newQty });
  };

  const handleAuthRemove = (itemId: string) => {
    removeMutation.mutate(itemId, {
      onSuccess: () => showToast.success('Đã xóa món khỏi giỏ hàng'),
    });
  };

  if (!isOpen) return null;

  return (
    <>
      {/* Overlay Backdrop */}
      <div
        className="fixed inset-0 bg-black/40 backdrop-blur-xs z-50 transition-opacity animate-fade-in"
        onClick={onClose}
        aria-hidden="true"
      />

      {/* Drawer Panel */}
      <div 
        role="dialog"
        aria-modal="true"
        aria-label="Giỏ hàng của bạn"
        className="fixed right-0 top-0 h-full w-full sm:w-[440px] bg-[#FFFCF7] shadow-2xl z-50 flex flex-col border-l border-[#E8E1D7] animate-slide-in-right"
      >
        {/* Drawer Header */}
        <div className="flex items-center justify-between px-6 py-5 border-b border-[#E8E1D7] bg-white">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-xl bg-[#0F6B4F]/10 text-[#0F6B4F] flex items-center justify-center font-bold">
              <ShoppingBag size={20} />
            </div>
            <div>
              <h2 className="font-extrabold text-[#1F2937] text-base leading-tight">Giỏ Hàng</h2>
              <span className="text-[11px] text-[#6B7280]">
                {isAuthenticated 
                  ? `${cart?.items?.length || 0} món ăn`
                  : `${guestItems.length} món ăn`}
              </span>
            </div>
          </div>

          <button
            onClick={onClose}
            className="w-9 h-9 rounded-xl text-[#6B7280] hover:text-[#1F2937] hover:bg-[#FFF9F0] flex items-center justify-center transition-colors border border-transparent hover:border-[#E8E1D7]"
            aria-label="Đóng giỏ hàng"
          >
            <X size={20} />
          </button>
        </div>

        {/* Drawer Content Body */}
        <div className="flex-1 overflow-y-auto p-5 space-y-4">
          {isAuthenticated ? (
            <AuthCartContent
              cart={cart}
              isLoading={isLoading}
              onQtyChange={handleAuthQtyChange}
              onRemove={handleAuthRemove}
              isUpdating={updateMutation.isPending || removeMutation.isPending}
              onClose={onClose}
            />
          ) : (
            <GuestCartContent
              items={guestItems}
              onQtyChange={guestUpdateQty}
              onRemove={guestRemove}
              onClose={onClose}
            />
          )}
        </div>

        {/* Drawer Sticky Footer */}
        <CartDrawerFooter
          isAuthenticated={isAuthenticated}
          cart={cart}
          guestItems={guestItems}
          onClose={onClose}
        />
      </div>
    </>
  );
}

// Authenticated cart content
function AuthCartContent({
  cart,
  isLoading,
  onQtyChange,
  onRemove,
  isUpdating,
  onClose,
}: {
  cart?: CartResponse;
  isLoading: boolean;
  onQtyChange: (id: string, qty: number) => void;
  onRemove: (id: string) => void;
  isUpdating: boolean;
  onClose: () => void;
}) {
  if (isLoading) {
    return (
      <div className="space-y-3 py-4">
        {[1, 2, 3].map((i) => (
          <div key={i} className="p-3.5 rounded-2xl bg-white border border-[#E8E1D7] flex gap-3 animate-pulse">
            <div className="w-16 h-16 bg-stone-200 rounded-xl shrink-0" />
            <div className="flex-1 space-y-2">
              <div className="h-4 bg-stone-200 rounded w-2/3" />
              <div className="h-3 bg-stone-200 rounded w-1/2" />
              <div className="h-4 bg-stone-200 rounded w-1/3 pt-2" />
            </div>
          </div>
        ))}
      </div>
    );
  }

  if (!cart || !cart.items || cart.items.length === 0) {
    return <EmptyCartState onClose={onClose} />;
  }

  return (
    <div className="space-y-3">
      {cart.items.map((item) => (
        <AuthCartItemCard
          key={item.id}
          item={item}
          onQtyChange={onQtyChange}
          onRemove={onRemove}
          isUpdating={isUpdating}
        />
      ))}
    </div>
  );
}

function AuthCartItemCard({
  item,
  onQtyChange,
  onRemove,
  isUpdating,
}: {
  item: CartItemResponse;
  onQtyChange: (id: string, qty: number) => void;
  onRemove: (id: string) => void;
  isUpdating: boolean;
}) {
  return (
    <div
      className={`p-3.5 rounded-2xl border transition-all ${
        !item.isValid
          ? 'border-red-200 bg-red-50/40'
          : item.priceChanged
          ? 'border-amber-200 bg-amber-50/40'
          : 'border-[#E8E1D7] bg-white hover:border-[#0F6B4F]'
      }`}
    >
      <div className="flex gap-3">
        {item.imageUrl ? (
          <div className="relative w-16 h-16 rounded-xl overflow-hidden border border-[#E8E1D7] shrink-0 bg-stone-50">
            <Image
              src={item.imageUrl}
              alt={item.productName}
              fill
              className="object-cover"
              sizes="64px"
            />
          </div>
        ) : (
          <div className="w-16 h-16 bg-[#FFF9F0] rounded-xl flex items-center justify-center text-2xl border border-[#E8E1D7] shrink-0">
            🥣
          </div>
        )}

        <div className="flex-1 min-w-0 flex flex-col justify-between">
          <div>
            <div className="flex items-start justify-between gap-1">
              <h4 className="font-bold text-[#1F2937] text-sm truncate">
                {item.productName}
              </h4>
              <button
                onClick={() => onRemove(item.id)}
                disabled={isUpdating}
                className="p-1 text-[#6B7280] hover:text-red-600 transition-colors shrink-0 rounded-lg hover:bg-red-50"
                title="Xóa món"
              >
                <Trash2 size={15} />
              </button>
            </div>

            {item.options && item.options.length > 0 && (
              <p className="text-[11px] text-[#6B7280] mt-0.5 truncate">
                {item.options.map((o) => o.optionName).join(', ')}
              </p>
            )}

            {item.specialNote && (
              <p className="text-[11px] text-amber-700 italic mt-0.5 truncate">
                📝 {item.specialNote}
              </p>
            )}

            {item.priceChanged && (
              <div className="flex items-center gap-1 mt-1 text-[10px] text-amber-700 font-bold">
                <AlertTriangle size={11} />
                Giá món đã cập nhật
              </div>
            )}

            {!item.isValid && (
              <div className="flex items-center gap-1 mt-1 text-[10px] text-red-600 font-bold">
                <AlertTriangle size={11} />
                {item.validationErrors?.[0] || 'Tùy chọn không còn khả dụng'}
              </div>
            )}
          </div>

          <div className="flex items-center justify-between mt-2 pt-1 border-t border-[#E8E1D7]/50">
            <div className="flex items-center gap-1 bg-[#FFF9F0] rounded-xl border border-[#E8E1D7] p-0.5">
              <button
                onClick={() => onQtyChange(item.id, item.quantity - 1)}
                disabled={isUpdating || item.quantity <= 1}
                className="w-6 h-6 rounded-lg flex items-center justify-center text-[#1F2937] hover:bg-white disabled:opacity-40 transition-all"
                aria-label="Giảm số lượng"
              >
                <Minus size={12} />
              </button>
              <span className="w-6 text-center text-xs font-extrabold text-[#1F2937]">
                {item.quantity}
              </span>
              <button
                onClick={() => onQtyChange(item.id, item.quantity + 1)}
                disabled={isUpdating || item.quantity >= 99}
                className="w-6 h-6 rounded-lg flex items-center justify-center text-[#1F2937] hover:bg-white disabled:opacity-40 transition-all"
                aria-label="Tăng số lượng"
              >
                <Plus size={12} />
              </button>
            </div>

            <div className="text-right">
              <span className="text-xs text-[#6B7280] block text-[10px]">Thành tiền</span>
              <span className="text-sm font-black text-[#0F6B4F]">
                {item.lineTotal.toLocaleString('vi-VN')}đ
              </span>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

// Guest cart content
function GuestCartContent({
  items,
  onQtyChange,
  onRemove,
  onClose,
}: {
  items: GuestCartItem[];
  onQtyChange: (clientItemId: string, quantity: number) => void;
  onRemove: (clientItemId: string) => void;
  onClose: () => void;
}) {
  if (items.length === 0) {
    return <EmptyCartState onClose={onClose} />;
  }

  return (
    <div className="space-y-3">
      {items.map((item) => {
        const lineTotal = item.displayPrice * item.quantity;
        return (
          <div
            key={item.clientItemId}
            className="p-3.5 rounded-2xl border border-[#E8E1D7] bg-white hover:border-[#0F6B4F] transition-all"
          >
            <div className="flex gap-3">
              {item.imageUrl ? (
                <div className="relative w-16 h-16 rounded-xl overflow-hidden border border-[#E8E1D7] shrink-0 bg-stone-50">
                  <Image
                    src={item.imageUrl}
                    alt={item.productName}
                    fill
                    className="object-cover"
                    sizes="64px"
                  />
                </div>
              ) : (
                <div className="w-16 h-16 bg-[#FFF9F0] rounded-xl flex items-center justify-center text-2xl border border-[#E8E1D7] shrink-0">
                  🥣
                </div>
              )}

              <div className="flex-1 min-w-0 flex flex-col justify-between">
                <div>
                  <div className="flex items-start justify-between gap-1">
                    <h4 className="font-bold text-[#1F2937] text-sm truncate">
                      {item.productName}
                    </h4>
                    <button
                      onClick={() => {
                        onRemove(item.clientItemId);
                        showToast.success('Đã xóa món khỏi giỏ hàng');
                      }}
                      className="p-1 text-[#6B7280] hover:text-red-600 transition-colors shrink-0 rounded-lg hover:bg-red-50"
                      aria-label="Xóa món"
                    >
                      <Trash2 size={15} />
                    </button>
                  </div>

                  {item.optionNames && item.optionNames.length > 0 && (
                    <p className="text-[11px] text-[#6B7280] mt-0.5 truncate">
                      {item.optionNames.join(', ')}
                    </p>
                  )}

                  {item.specialNote && (
                    <p className="text-[11px] text-amber-700 italic mt-0.5 truncate">
                      📝 {item.specialNote}
                    </p>
                  )}
                </div>

                <div className="flex items-center justify-between mt-2 pt-1 border-t border-[#E8E1D7]/50">
                  <div className="flex items-center gap-1 bg-[#FFF9F0] rounded-xl border border-[#E8E1D7] p-0.5">
                    <button
                      onClick={() => onQtyChange(item.clientItemId, item.quantity - 1)}
                      disabled={item.quantity <= 1}
                      className="w-6 h-6 rounded-lg flex items-center justify-center text-[#1F2937] hover:bg-white disabled:opacity-40 transition-all"
                      aria-label="Giảm số lượng"
                    >
                      <Minus size={12} />
                    </button>
                    <span className="w-6 text-center text-xs font-extrabold text-[#1F2937]">
                      {item.quantity}
                    </span>
                    <button
                      onClick={() => onQtyChange(item.clientItemId, item.quantity + 1)}
                      disabled={item.quantity >= 99}
                      className="w-6 h-6 rounded-lg flex items-center justify-center text-[#1F2937] hover:bg-white disabled:opacity-40 transition-all"
                      aria-label="Tăng số lượng"
                    >
                      <Plus size={12} />
                    </button>
                  </div>

                  <div className="text-right">
                    <span className="text-xs text-[#6B7280] block text-[10px]">Thành tiền</span>
                    <span className="text-sm font-black text-[#0F6B4F]">
                      {lineTotal.toLocaleString('vi-VN')}đ
                    </span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        );
      })}

      <div className="p-3.5 rounded-xl bg-[#FAF0D1] border border-[#C7A45B]/40 text-[#5E4A22]">
        <p className="text-xs font-semibold leading-relaxed">
          💡 Đăng nhập để lưu giỏ hàng lâu dài và nhận nhiều voucher ưu đãi hấp dẫn.
        </p>
      </div>
    </div>
  );
}

// Empty Cart State
function EmptyCartState({ onClose }: { onClose: () => void }) {
  return (
    <div className="flex flex-col items-center justify-center py-16 text-center space-y-4 my-auto">
      <div className="w-20 h-20 bg-[#FFF9F0] border border-[#E8E1D7] rounded-full flex items-center justify-center text-[#0F6B4F] shadow-sm">
        <ShoppingBag size={36} />
      </div>
      <div className="space-y-1 max-w-xs">
        <h3 className="text-base font-extrabold text-[#1F2937]">Giỏ hàng của bạn đang trống</h3>
        <p className="text-xs text-[#6B7280] leading-relaxed">
          Hãy khám phá những tô phở bò thơm ngon chuẩn vị Hà Nội và món ăn kèm hấp dẫn từ thực đơn nhé!
        </p>
      </div>
      <div className="pt-2">
        <Link href="/menu" onClick={onClose}>
          <Button className="h-11 px-6 bg-[#0F6B4F] hover:bg-[#084C38] text-white font-bold text-xs rounded-xl gap-2 shadow-md">
            Khám phá thực đơn
            <ArrowRight size={16} />
          </Button>
        </Link>
      </div>
    </div>
  );
}

// Sticky Footer Component
function CartDrawerFooter({
  isAuthenticated,
  cart,
  guestItems,
  onClose,
}: {
  isAuthenticated: boolean;
  cart?: CartResponse;
  guestItems: GuestCartItem[];
  onClose: () => void;
}) {
  const hasItems = isAuthenticated
    ? (cart?.items?.length || 0) > 0
    : guestItems.length > 0;

  if (!hasItems) return null;

  const subtotal = isAuthenticated
    ? cart?.subtotal || 0
    : guestItems.reduce((sum, it) => sum + (it.displayPrice * it.quantity), 0);

  const discount = isAuthenticated ? cart?.discountAmount || 0 : 0;
  const total = isAuthenticated ? cart?.estimatedTotal || 0 : subtotal;

  return (
    <div className="border-t border-[#E8E1D7] p-5 space-y-4 bg-white shadow-lg mt-auto">
      <div className="space-y-2 text-xs text-[#6B7280]">
        <div className="flex justify-between">
          <span>Tạm tính</span>
          <span className="font-bold text-[#1F2937]">{subtotal.toLocaleString('vi-VN')}đ</span>
        </div>
        {discount > 0 && (
          <div className="flex justify-between text-[#0F6B4F]">
            <span>Giảm giá voucher</span>
            <span className="font-bold">-{discount.toLocaleString('vi-VN')}đ</span>
          </div>
        )}
        <div className="flex justify-between text-[#1F2937] font-extrabold text-base pt-2 border-t border-[#E8E1D7]">
          <span>Tổng tiền</span>
          <span className="text-[#0F6B4F]">{total.toLocaleString('vi-VN')}đ</span>
        </div>
      </div>

      {isAuthenticated ? (
        <div className="grid grid-cols-2 gap-3">
          <Link href="/customer/cart" onClick={onClose} className="block">
            <Button variant="outline" className="w-full h-11 border-[#0F6B4F] text-[#0F6B4F] hover:bg-[#0F6B4F] hover:text-white font-bold text-xs rounded-xl transition-all">
              Xem giỏ hàng
            </Button>
          </Link>
          <Link href="/customer/checkout" onClick={onClose} className="block">
            <Button className="w-full h-11 bg-[#0F6B4F] hover:bg-[#084C38] text-white font-bold text-xs rounded-xl shadow-md gap-1.5 transition-all">
              Thanh toán
              <ArrowRight size={14} />
            </Button>
          </Link>
        </div>
      ) : (
        <div className="space-y-2">
          <Link href="/login" onClick={onClose} className="block">
            <Button className="w-full h-11 bg-[#0F6B4F] hover:bg-[#084C38] text-white font-bold text-xs rounded-xl shadow-md gap-1.5">
              Đăng nhập để thanh toán
              <ArrowRight size={14} />
            </Button>
          </Link>
        </div>
      )}
    </div>
  );
}
