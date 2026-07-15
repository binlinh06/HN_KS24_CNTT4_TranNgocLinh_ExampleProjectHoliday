'use client';

import React from 'react';
import Link from 'next/link';
import { X, ShoppingBag, Trash2, Minus, Plus, AlertTriangle } from 'lucide-react';
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

  const handleAuthQtyChange = (itemId: string, newQty: number) => {
    if (newQty < 1) return;
    if (newQty > 99) {
      showToast.error('Số lượng tối đa là 99');
      return;
    }
    updateMutation.mutate({ cartItemId: itemId, quantity: newQty });
  };

  const handleAuthRemove = (itemId: string) => {
    removeMutation.mutate(itemId);
  };

  if (!isOpen) return null;

  return (
    <>
      {/* Overlay */}
      <div
        className="fixed inset-0 bg-black/30 backdrop-blur-sm z-50 transition-opacity"
        onClick={onClose}
      />

      {/* Drawer */}
      <div className="fixed right-0 top-0 h-full w-full max-w-md bg-white shadow-2xl z-50 flex flex-col animate-slide-in-right">
        {/* Header */}
        <div className="flex items-center justify-between px-5 py-4 border-b border-stone-200">
          <div className="flex items-center gap-2">
            <ShoppingBag size={20} className="text-primary-600" />
            <h2 className="font-bold text-stone-900 text-lg">Giỏ hàng</h2>
          </div>
          <button
            onClick={onClose}
            className="p-1.5 rounded-full text-stone-400 hover:text-stone-600 hover:bg-stone-100 transition-colors"
          >
            <X size={20} />
          </button>
        </div>

        {/* Content */}
        <div className="flex-1 overflow-y-auto p-5">
          {isAuthenticated ? (
            <AuthCartContent
              cart={cart}
              isLoading={isLoading}
              onQtyChange={handleAuthQtyChange}
              onRemove={handleAuthRemove}
              isUpdating={updateMutation.isPending || removeMutation.isPending}
            />
          ) : (
            <GuestCartContent
              items={guestItems}
              onQtyChange={guestUpdateQty}
              onRemove={guestRemove}
            />
          )}
        </div>

        {/* Footer */}
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
}: {
  cart?: CartResponse;
  isLoading: boolean;
  onQtyChange: (id: string, qty: number) => void;
  onRemove: (id: string) => void;
  isUpdating: boolean;
}) {
  if (isLoading) {
    return (
      <div className="flex items-center justify-center py-16">
        <div className="w-8 h-8 border-2 border-stone-200 border-t-primary-500 rounded-full animate-spin" />
      </div>
    );
  }

  if (!cart || cart.items.length === 0) {
    return (
      <div className="flex flex-col items-center justify-center py-16 text-center">
        <div className="w-16 h-16 bg-stone-100 rounded-full flex items-center justify-center mb-4">
          <ShoppingBag size={28} className="text-stone-400" />
        </div>
        <p className="text-stone-500 text-sm font-medium">Giỏ hàng trống</p>
        <p className="text-stone-400 text-xs mt-1">Hãy thêm món từ thực đơn</p>
      </div>
    );
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
      className={`p-3 rounded-xl border transition-all ${
        !item.isValid
          ? 'border-red-200 bg-red-50/50'
          : item.priceChanged
          ? 'border-amber-200 bg-amber-50/50'
          : 'border-stone-200 bg-white'
      }`}
    >
      <div className="flex gap-3">
        {item.imageUrl ? (
          <img
            src={item.imageUrl}
            alt={item.productName}
            className="w-14 h-14 rounded-lg object-cover border border-stone-100 shrink-0"
          />
        ) : (
          <div className="w-14 h-14 bg-stone-50 rounded-lg flex items-center justify-center text-2xl border border-stone-100 shrink-0">
            🥣
          </div>
        )}
        <div className="flex-1 min-w-0">
          <div className="flex items-start justify-between">
            <h4 className="font-bold text-stone-900 text-sm truncate pr-2">
              {item.productName}
            </h4>
            <button
              onClick={() => onRemove(item.id)}
              disabled={isUpdating}
              className="p-1 text-stone-400 hover:text-red-500 transition-colors shrink-0"
            >
              <Trash2 size={14} />
            </button>
          </div>

          {item.options.length > 0 && (
            <p className="text-[11px] text-stone-500 mt-0.5 truncate">
              {item.options.map((o) => o.optionName).join(', ')}
            </p>
          )}

          {item.specialNote && (
            <p className="text-[11px] text-stone-400 italic mt-0.5 truncate">
              📝 {item.specialNote}
            </p>
          )}

          {item.priceChanged && (
            <div className="flex items-center gap-1 mt-1 text-[10px] text-amber-600 font-medium">
              <AlertTriangle size={10} />
              Giá đã thay đổi
            </div>
          )}

          {!item.isValid && (
            <div className="flex items-center gap-1 mt-1 text-[10px] text-red-600 font-medium">
              <AlertTriangle size={10} />
              {item.validationErrors[0] || 'Cấu hình không còn khả dụng'}
            </div>
          )}

          <div className="flex items-center justify-between mt-2">
            <div className="flex items-center gap-1 bg-stone-100 rounded-lg">
              <button
                onClick={() => onQtyChange(item.id, item.quantity - 1)}
                disabled={isUpdating || item.quantity <= 1}
                className="p-1 text-stone-500 hover:text-stone-900 disabled:opacity-40 transition-colors"
              >
                <Minus size={13} />
              </button>
              <span className="w-6 text-center text-xs font-bold text-stone-800">
                {item.quantity}
              </span>
              <button
                onClick={() => onQtyChange(item.id, item.quantity + 1)}
                disabled={isUpdating || item.quantity >= 99}
                className="p-1 text-stone-500 hover:text-stone-900 disabled:opacity-40 transition-colors"
              >
                <Plus size={13} />
              </button>
            </div>
            <span className="text-sm font-extrabold text-primary-700">
              {item.lineTotal.toLocaleString('vi-VN')}đ
            </span>
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
}: {
  items: GuestCartItem[];
  onQtyChange: (clientItemId: string, quantity: number) => void;
  onRemove: (clientItemId: string) => void;
}) {
  if (items.length === 0) {
    return (
      <div className="flex flex-col items-center justify-center py-16 text-center">
        <div className="w-16 h-16 bg-stone-100 rounded-full flex items-center justify-center mb-4">
          <ShoppingBag size={28} className="text-stone-400" />
        </div>
        <p className="text-stone-500 text-sm font-medium">Giỏ hàng trống</p>
        <p className="text-stone-400 text-xs mt-1">Hãy thêm món từ thực đơn</p>
      </div>
    );
  }

  return (
    <div className="space-y-3">
      {items.map((item) => (
        <div
          key={item.clientItemId}
          className="p-3 rounded-xl border border-stone-200 bg-white"
        >
          <div className="flex gap-3">
            {item.imageUrl ? (
              <img
                src={item.imageUrl}
                alt={item.productName}
                className="w-14 h-14 rounded-lg object-cover border border-stone-100 shrink-0"
              />
            ) : (
              <div className="w-14 h-14 bg-stone-50 rounded-lg flex items-center justify-center text-2xl border border-stone-100 shrink-0">
                🥣
              </div>
            )}
            <div className="flex-1 min-w-0">
              <div className="flex items-start justify-between">
                <h4 className="font-bold text-stone-900 text-sm truncate pr-2">
                  {item.productName}
                </h4>
                <button
                  onClick={() => onRemove(item.clientItemId)}
                  className="p-1 text-stone-400 hover:text-red-500 transition-colors shrink-0"
                >
                  <Trash2 size={14} />
                </button>
              </div>

              {item.optionNames.length > 0 && (
                <p className="text-[11px] text-stone-500 mt-0.5 truncate">
                  {item.optionNames.join(', ')}
                </p>
              )}

              {item.specialNote && (
                <p className="text-[11px] text-stone-400 italic mt-0.5 truncate">
                  📝 {item.specialNote}
                </p>
              )}

              <div className="flex items-center justify-between mt-2">
                <div className="flex items-center gap-1 bg-stone-100 rounded-lg">
                  <button
                    onClick={() =>
                      onQtyChange(item.clientItemId, item.quantity - 1)
                    }
                    disabled={item.quantity <= 1}
                    className="p-1 text-stone-500 hover:text-stone-900 disabled:opacity-40 transition-colors"
                  >
                    <Minus size={13} />
                  </button>
                  <span className="w-6 text-center text-xs font-bold text-stone-800">
                    {item.quantity}
                  </span>
                  <button
                    onClick={() =>
                      onQtyChange(item.clientItemId, item.quantity + 1)
                    }
                    disabled={item.quantity >= 99}
                    className="p-1 text-stone-500 hover:text-stone-900 disabled:opacity-40 transition-colors"
                  >
                    <Plus size={13} />
                  </button>
                </div>
                <span className="text-sm font-extrabold text-primary-700">
                  {item.displayPrice.toLocaleString('vi-VN')}đ
                </span>
              </div>
            </div>
          </div>
        </div>
      ))}

      <div className="p-3 rounded-lg bg-amber-50 border border-amber-200">
        <p className="text-xs text-amber-700 font-medium">
          💡 Đăng nhập để lưu giỏ hàng và thanh toán. Giá hiển thị chỉ mang tính tham khảo.
        </p>
      </div>
    </div>
  );
}

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
    ? (cart?.items.length || 0) > 0
    : guestItems.length > 0;

  if (!hasItems) return null;

  const subtotal = isAuthenticated
    ? cart?.subtotal || 0
    : guestItems.reduce((sum, it) => sum + it.displayPrice, 0);

  const discount = isAuthenticated ? cart?.discountAmount || 0 : 0;
  const total = isAuthenticated ? cart?.estimatedTotal || 0 : subtotal;

  return (
    <div className="border-t border-stone-200 p-5 space-y-3 bg-stone-50/50">
      <div className="space-y-1.5 text-sm">
        <div className="flex justify-between text-stone-600">
          <span>Tạm tính</span>
          <span className="font-semibold">{subtotal.toLocaleString('vi-VN')}đ</span>
        </div>
        {discount > 0 && (
          <div className="flex justify-between text-green-600">
            <span>Giảm giá</span>
            <span className="font-semibold">-{discount.toLocaleString('vi-VN')}đ</span>
          </div>
        )}
        <div className="flex justify-between text-stone-900 font-bold text-base pt-1 border-t border-stone-200">
          <span>Tổng cộng</span>
          <span className="text-primary-700">{total.toLocaleString('vi-VN')}đ</span>
        </div>
      </div>

      {isAuthenticated ? (
        <div className="flex gap-2">
          <Link href="/customer/cart" className="flex-1" onClick={onClose}>
            <Button variant="outline" className="w-full text-sm">
              Xem giỏ hàng
            </Button>
          </Link>
          <Button disabled className="flex-1 text-sm opacity-60" title="Sẽ hoàn thiện ở Giai đoạn 5">
            Thanh toán
          </Button>
        </div>
      ) : (
        <Link href="/login" onClick={onClose}>
          <Button className="w-full text-sm">
            Đăng nhập để thanh toán
          </Button>
        </Link>
      )}
    </div>
  );
}
