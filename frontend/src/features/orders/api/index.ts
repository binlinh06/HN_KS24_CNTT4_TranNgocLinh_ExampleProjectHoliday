import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { api } from '@/lib/api';
import { ApiResponse, OrderResponse, OrderSummaryResponse, OrderTrackingResponse, ReviewRequest, ReviewResponse } from '@/types';

// Query Keys
export const orderKeys = {
  all: ['orders'] as const,
  history: (params: Record<string, any>) => [...orderKeys.all, 'history', params] as const,
  details: (id: string) => [...orderKeys.all, 'details', id] as const,
  tracking: (id: string) => [...orderKeys.all, 'tracking', id] as const,
  review: (orderId: string) => [...orderKeys.all, 'review', orderId] as const,
};

// ============ API Functions ============

export const getOrdersHistory = async (params: Record<string, any>): Promise<{ content: OrderSummaryResponse[]; page: any }> => {
  const response = await api.get<ApiResponse<{ content: OrderSummaryResponse[]; page: any }>>('/orders', { params });
  return response.data.data!;
};

export const getOrderDetails = async (orderId: string): Promise<OrderResponse> => {
  const response = await api.get<ApiResponse<OrderResponse>>(`/orders/${orderId}`);
  return response.data.data!;
};

export const getOrderTracking = async (orderId: string): Promise<OrderTrackingResponse> => {
  const response = await api.get<ApiResponse<OrderTrackingResponse>>(`/orders/${orderId}/tracking`);
  return response.data.data!;
};

export const createReview = async (orderId: string, data: ReviewRequest): Promise<ReviewResponse> => {
  const response = await api.post<ApiResponse<ReviewResponse>>(`/orders/${orderId}/review`, data);
  return response.data.data!;
};

export const getOrderReview = async (orderId: string): Promise<ReviewResponse | null> => {
  try {
    const response = await api.get<ApiResponse<ReviewResponse>>(`/orders/${orderId}/review`);
    return response.data.data || null;
  } catch (error: any) {
    // If review not found, return null instead of throwing
    if (error.response?.status === 404) {
      return null;
    }
    throw error;
  }
};

// ============ TanStack Query Hooks ============

export const useOrdersHistory = (params: Record<string, any>) => {
  return useQuery({
    queryKey: orderKeys.history(params),
    queryFn: () => getOrdersHistory(params),
  });
};

export const useOrderDetails = (orderId: string) => {
  return useQuery({
    queryKey: orderKeys.details(orderId),
    queryFn: () => getOrderDetails(orderId),
    enabled: !!orderId,
  });
};

export const useOrderTrackingQuery = (orderId: string) => {
  return useQuery({
    queryKey: orderKeys.tracking(orderId),
    queryFn: () => getOrderTracking(orderId),
    enabled: !!orderId,
  });
};

export const useOrderReview = (orderId: string) => {
  return useQuery({
    queryKey: orderKeys.review(orderId),
    queryFn: () => getOrderReview(orderId),
    enabled: !!orderId,
  });
};

export const useCreateReview = (orderId: string) => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (data: ReviewRequest) => createReview(orderId, data),
    onSuccess: () => {
      // Invalidate both details and review queries
      queryClient.invalidateQueries({ queryKey: orderKeys.details(orderId) });
      queryClient.invalidateQueries({ queryKey: orderKeys.review(orderId) });
      queryClient.invalidateQueries({ queryKey: orderKeys.all });
    },
  });
};
