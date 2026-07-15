'use client';

import React, { useState, useEffect } from 'react';
import { ShoppingBag } from 'lucide-react';
import { useAuthStore } from '@/stores/auth';
import { useGuestCartStore } from '@/stores/cart';
import { useCart } from '@/features/cart/api';
import { CartDrawer } from '@/components/cart/cart-drawer';

export function CartBadge() {
  const [isDrawerOpen, setIsDrawerOpen] = useState(false);
  const { isAuthenticated } = useAuthStore();

  // Load guest cart from localStorage on mount
  const loadFromStorage = useGuestCartStore((s) => s.loadFromStorage);
  const guestItems = useGuestCartStore((s) => s.items);

  useEffect(() => {
    loadFromStorage();
  }, [loadFromStorage]);

  // Authenticated cart count
  const { data: cart } = useCart(isAuthenticated);
  const authCount = cart?.itemCount || 0;
  const guestCount = guestItems.reduce((sum, it) => sum + it.quantity, 0);

  const totalCount = isAuthenticated ? authCount : guestCount;

  return (
    <>
      <button
        onClick={() => setIsDrawerOpen(true)}
        className="relative p-1.5 rounded-full text-stone-500 hover:text-primary-600 hover:bg-stone-50 transition-colors focus:outline-none"
        aria-label="Giỏ hàng"
      >
        <ShoppingBag size={20} />
        {totalCount > 0 && (
          <span className="absolute -top-1 -right-1 min-w-[18px] h-[18px] flex items-center justify-center bg-primary-500 text-white text-[10px] font-bold rounded-full px-1 leading-none shadow-sm">
            {totalCount > 99 ? '99+' : totalCount}
          </span>
        )}
      </button>

      <CartDrawer isOpen={isDrawerOpen} onClose={() => setIsDrawerOpen(false)} />
    </>
  );
}
