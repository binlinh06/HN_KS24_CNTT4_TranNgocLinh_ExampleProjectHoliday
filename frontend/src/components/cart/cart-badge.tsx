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
        className="relative p-2 rounded-2xl border border-[#E8E1D7] text-[#1F2937] hover:text-[#0F6B4F] hover:bg-[#FFF9F0] hover:border-[#0F6B4F] transition-all focus:outline-none shadow-sm flex items-center justify-center"
        aria-label="Mở giỏ hàng"
      >
        <ShoppingBag size={20} />
        {totalCount > 0 && (
          <span className="absolute -top-1.5 -right-1.5 min-w-[20px] h-[20px] flex items-center justify-center bg-[#0F6B4F] text-white text-[10px] font-black rounded-full px-1 leading-none shadow-md border-2 border-white">
            {totalCount > 99 ? '99+' : totalCount}
          </span>
        )}
      </button>

      <CartDrawer isOpen={isDrawerOpen} onClose={() => setIsDrawerOpen(false)} />
    </>
  );
}
