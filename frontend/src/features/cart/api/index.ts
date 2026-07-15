import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { api } from '@/lib/api';
import { ApiResponse, CartResponse, CartMergeResponse } from '@/types';

// Query Keys
export const cartKeys = {
  all: ['cart'] as const,
  detail: () => [...cartKeys.all, 'detail'] as const,
};

// ============ API Functions ============

export const getCart = async (): Promise<CartResponse> => {
  const response = await api.get<ApiResponse<CartResponse>>('/cart');
  return response.data.data!;
};

export interface AddCartItemPayload {
  productId: string;
  optionIds?: string[];
  quantity: number;
  specialNote?: string;
}

export const addCartItem = async (data: AddCartItemPayload): Promise<CartResponse> => {
  const response = await api.post<ApiResponse<CartResponse>>('/cart/items', data);
  return response.data.data!;
};

export interface UpdateCartItemPayload {
  cartItemId: string;
  quantity?: number;
  optionIds?: string[];
  specialNote?: string;
}

export const updateCartItem = async ({
  cartItemId,
  ...data
}: UpdateCartItemPayload): Promise<CartResponse> => {
  const response = await api.patch<ApiResponse<CartResponse>>(
    `/cart/items/${cartItemId}`,
    data
  );
  return response.data.data!;
};

export const removeCartItem = async (cartItemId: string): Promise<CartResponse> => {
  const response = await api.delete<ApiResponse<CartResponse>>(
    `/cart/items/${cartItemId}`
  );
  return response.data.data!;
};

export const clearCart = async (): Promise<CartResponse> => {
  const response = await api.delete<ApiResponse<CartResponse>>('/cart');
  return response.data.data!;
};

export const applyVoucher = async (code: string): Promise<CartResponse> => {
  const response = await api.post<ApiResponse<CartResponse>>('/cart/voucher', {
    code,
  });
  return response.data.data!;
};

export const removeVoucher = async (): Promise<CartResponse> => {
  const response = await api.delete<ApiResponse<CartResponse>>('/cart/voucher');
  return response.data.data!;
};

export interface MergeCartPayload {
  items: {
    clientItemId: string;
    productId: string;
    optionIds: string[];
    quantity: number;
    specialNote?: string;
  }[];
  idempotencyKey: string;
}

export const mergeCart = async ({
  items,
  idempotencyKey,
}: MergeCartPayload): Promise<CartMergeResponse> => {
  const response = await api.post<ApiResponse<CartMergeResponse>>(
    '/cart/merge',
    items,
    {
      headers: { 'Idempotency-Key': idempotencyKey },
    }
  );
  return response.data.data!;
};

// ============ React Query Hooks ============

export const useCart = (enabled = true) => {
  return useQuery({
    queryKey: cartKeys.detail(),
    queryFn: getCart,
    enabled,
    staleTime: 30 * 1000, // 30s stale time for cart
  });
};

export const useAddCartItem = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: addCartItem,
    onSuccess: (data) => {
      queryClient.setQueryData(cartKeys.detail(), data);
    },
  });
};

export const useUpdateCartItem = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: updateCartItem,
    onSuccess: (data) => {
      queryClient.setQueryData(cartKeys.detail(), data);
    },
  });
};

export const useRemoveCartItem = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: removeCartItem,
    onSuccess: (data) => {
      queryClient.setQueryData(cartKeys.detail(), data);
    },
  });
};

export const useClearCart = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: clearCart,
    onSuccess: (data) => {
      queryClient.setQueryData(cartKeys.detail(), data);
    },
  });
};

export const useApplyVoucher = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: applyVoucher,
    onSuccess: (data) => {
      queryClient.setQueryData(cartKeys.detail(), data);
    },
  });
};

export const useRemoveVoucher = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: removeVoucher,
    onSuccess: (data) => {
      queryClient.setQueryData(cartKeys.detail(), data);
    },
  });
};

export const useMergeCart = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: mergeCart,
    onSuccess: (data) => {
      queryClient.setQueryData(cartKeys.detail(), data.cart);
    },
  });
};
