import { create } from 'zustand';
import { GuestCartItem } from '@/types';

const GUEST_CART_KEY = 'guest_cart';
const MERGE_KEY_PREFIX = 'guest_cart_merge_key';

function loadGuestCart(): GuestCartItem[] {
  if (typeof window === 'undefined') return [];
  try {
    const raw = localStorage.getItem(GUEST_CART_KEY);
    return raw ? JSON.parse(raw) : [];
  } catch {
    return [];
  }
}

function saveGuestCart(items: GuestCartItem[]) {
  if (typeof window === 'undefined') return;
  try {
    localStorage.setItem(GUEST_CART_KEY, JSON.stringify(items));
  } catch {
    // Storage full or unavailable
  }
}

function generateClientItemId(): string {
  return crypto.randomUUID ? crypto.randomUUID() : `${Date.now()}-${Math.random().toString(36).slice(2, 10)}`;
}

interface GuestCartState {
  items: GuestCartItem[];
  mergeIdempotencyKey: string | null;
  hasMerged: boolean;

  // Actions
  loadFromStorage: () => void;
  addItem: (item: Omit<GuestCartItem, 'clientItemId'>) => void;
  updateQuantity: (clientItemId: string, quantity: number) => void;
  removeItem: (clientItemId: string) => void;
  clearAll: () => void;
  removeAcceptedItems: (acceptedClientItemIds: string[]) => void;
  getItemCount: () => number;
  getMergeKey: () => string;
  setHasMerged: (value: boolean) => void;
}

export const useGuestCartStore = create<GuestCartState>((set, get) => ({
  items: [],
  mergeIdempotencyKey: null,
  hasMerged: false,

  loadFromStorage: () => {
    const items = loadGuestCart();
    set({ items });
  },

  addItem: (item) => {
    const { items } = get();

    // Normalize for comparison
    const normalizedOptionIds = [...item.optionIds].sort();
    const normalizedNote = (item.specialNote || '').trim().toLowerCase();

    // Check for existing same-configuration item
    const existingIndex = items.findIndex((existing) => {
      const existingNormOptions = [...existing.optionIds].sort();
      const existingNormNote = (existing.specialNote || '').trim().toLowerCase();
      return (
        existing.productId === item.productId &&
        JSON.stringify(existingNormOptions) === JSON.stringify(normalizedOptionIds) &&
        existingNormNote === normalizedNote
      );
    });

    let updated: GuestCartItem[];
    if (existingIndex >= 0) {
      const newQty = items[existingIndex].quantity + item.quantity;
      if (newQty > 99) return; // reject silently
      updated = items.map((it, i) =>
        i === existingIndex ? { ...it, quantity: newQty } : it
      );
    } else {
      const newItem: GuestCartItem = {
        ...item,
        clientItemId: generateClientItemId(),
      };
      updated = [...items, newItem];
    }

    set({ items: updated });
    saveGuestCart(updated);
  },

  updateQuantity: (clientItemId, quantity) => {
    if (quantity < 1 || quantity > 99) return;
    const { items } = get();
    const updated = items.map((it) =>
      it.clientItemId === clientItemId ? { ...it, quantity } : it
    );
    set({ items: updated });
    saveGuestCart(updated);
  },

  removeItem: (clientItemId) => {
    const { items } = get();
    const updated = items.filter((it) => it.clientItemId !== clientItemId);
    set({ items: updated });
    saveGuestCart(updated);
  },

  clearAll: () => {
    set({ items: [], mergeIdempotencyKey: null });
    if (typeof window !== 'undefined') {
      localStorage.removeItem(GUEST_CART_KEY);
      localStorage.removeItem(MERGE_KEY_PREFIX);
    }
  },

  removeAcceptedItems: (acceptedClientItemIds) => {
    const { items } = get();
    const updated = items.filter(
      (it) => !acceptedClientItemIds.includes(it.clientItemId)
    );
    set({ items: updated });
    if (updated.length === 0) {
      // All items merged successfully — clean up
      if (typeof window !== 'undefined') {
        localStorage.removeItem(GUEST_CART_KEY);
        localStorage.removeItem(MERGE_KEY_PREFIX);
      }
    } else {
      saveGuestCart(updated);
    }
  },

  getItemCount: () => {
    return get().items.reduce((sum, it) => sum + it.quantity, 0);
  },

  getMergeKey: () => {
    // Reuse existing key for retry idempotency
    if (typeof window !== 'undefined') {
      const existing = localStorage.getItem(MERGE_KEY_PREFIX);
      if (existing) return existing;
      const newKey = crypto.randomUUID
        ? crypto.randomUUID()
        : `${Date.now()}-${Math.random().toString(36).slice(2, 10)}`;
      localStorage.setItem(MERGE_KEY_PREFIX, newKey);
      return newKey;
    }
    return `${Date.now()}-${Math.random().toString(36).slice(2, 10)}`;
  },

  setHasMerged: (value) => {
    set({ hasMerged: value });
    if (value && typeof window !== 'undefined') {
      localStorage.removeItem(MERGE_KEY_PREFIX);
    }
  },
}));
