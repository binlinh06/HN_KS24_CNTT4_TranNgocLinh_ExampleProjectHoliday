import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { api } from '@/lib/api';
import { ApiResponse, CheckoutPreviewRequest, CheckoutPreviewResponse, OrderResponse, PaymentResponse } from '@/types';

// Query Keys
export const checkoutKeys = {
  all: ['checkout'] as const,
  preview: (req: CheckoutPreviewRequest) => [...checkoutKeys.all, 'preview', req] as const,
  order: (id: string) => [...checkoutKeys.all, 'order', id] as const,
  payment: (orderId: string) => [...checkoutKeys.all, 'payment', orderId] as const,
};

// ============ API Functions ============

export const previewCheckout = async (data: CheckoutPreviewRequest): Promise<CheckoutPreviewResponse> => {
  const response = await api.post<ApiResponse<CheckoutPreviewResponse>>('/checkout/preview', data);
  return response.data.data!;
};

export const checkoutOrder = async ({
  data,
  idempotencyKey,
}: {
  data: CheckoutPreviewRequest;
  idempotencyKey: string;
}): Promise<OrderResponse> => {
  const response = await api.post<ApiResponse<OrderResponse>>('/orders/checkout', data, {
    headers: { 'Idempotency-Key': idempotencyKey },
  });
  return response.data.data!;
};

export const getOrderDetails = async (orderId: string): Promise<OrderResponse> => {
  const response = await api.get<ApiResponse<OrderResponse>>(`/orders/${orderId}`);
  return response.data.data!;
};

export const initiatePayment = async ({
  orderId,
  idempotencyKey,
}: {
  orderId: string;
  idempotencyKey: string;
}): Promise<PaymentResponse> => {
  const response = await api.post<ApiResponse<PaymentResponse>>(
    `/payments/orders/${orderId}/initiate`,
    {},
    { headers: { 'Idempotency-Key': idempotencyKey } }
  );
  return response.data.data!;
};

export const retryPayment = async ({
  orderId,
  idempotencyKey,
}: {
  orderId: string;
  idempotencyKey: string;
}): Promise<PaymentResponse> => {
  const response = await api.post<ApiResponse<PaymentResponse>>(
    `/payments/orders/${orderId}/retry`,
    {},
    { headers: { 'Idempotency-Key': idempotencyKey } }
  );
  return response.data.data!;
};

export const getPaymentDetails = async (orderId: string): Promise<PaymentResponse> => {
  const response = await api.get<ApiResponse<PaymentResponse>>(`/payments/orders/${orderId}`);
  return response.data.data!;
};

// ============ React Query Hooks ============

export const usePreviewCheckout = (req: CheckoutPreviewRequest, enabled = true) => {
  return useQuery({
    queryKey: checkoutKeys.preview(req),
    queryFn: () => previewCheckout(req),
    enabled: enabled && !!req.addressId && !!req.paymentMethod,
    staleTime: 5000,
  });
};

export const useCheckoutOrder = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: checkoutOrder,
    onSuccess: (data) => {
      queryClient.invalidateQueries({ queryKey: ['cart'] });
      queryClient.setQueryData(checkoutKeys.order(data.orderId), data);
    },
  });
};

export const useOrderDetails = (orderId: string, enabled = true) => {
  return useQuery({
    queryKey: checkoutKeys.order(orderId),
    queryFn: () => getOrderDetails(orderId),
    enabled: enabled && !!orderId,
  });
};

export const useInitiatePayment = () => {
  return useMutation({
    mutationFn: initiatePayment,
  });
};

export const useRetryPayment = () => {
  return useMutation({
    mutationFn: retryPayment,
  });
};

export const usePaymentDetails = (orderId: string, enabled = true) => {
  return useQuery({
    queryKey: checkoutKeys.payment(orderId),
    queryFn: () => getPaymentDetails(orderId),
    enabled: enabled && !!orderId,
  });
};

// ============ Dev Mock Payment Simulation ============

export const simulateMockPayment = async ({
  paymentId,
  status,
}: {
  paymentId: string;
  status: 'SUCCESS' | 'FAILED' | 'CANCELED' | 'EXPIRED';
}): Promise<PaymentResponse> => {
  const response = await api.post<ApiResponse<PaymentResponse>>(
    `/dev/mock-payments/${paymentId}/simulate`,
    { status }
  );
  return response.data.data!;
};

export const useSimulateMockPayment = () => {
  return useMutation({
    mutationFn: simulateMockPayment,
  });
};
