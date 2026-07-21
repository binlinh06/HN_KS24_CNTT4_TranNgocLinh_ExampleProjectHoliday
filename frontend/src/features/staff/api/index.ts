import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { api } from '@/lib/api';
import { ApiResponse, OrderResponse, RestaurantTable } from '@/types';

// Query Keys for Staff Operations
export const staffKeys = {
  all: ['staff'] as const,
  orders: (params: Record<string, any>) => [...staffKeys.all, 'orders', params] as const,
  orderDetails: (id: string) => [...staffKeys.all, 'orderDetails', id] as const,
  tables: ['staff', 'tables'] as const,
  kitchenQueue: ['staff', 'kitchenQueue'] as const,
  invoice: (id: string) => [...staffKeys.all, 'invoice', id] as const,
  invoiceByOrder: (orderId: string) => [...staffKeys.all, 'invoiceByOrder', orderId] as const,
};

// ============ Staff Order API ============

export const getStaffOrders = async (params: Record<string, any>): Promise<OrderResponse[]> => {
  const response = await api.get<ApiResponse<OrderResponse[]>>('/staff/orders', { params });
  return response.data.data || [];
};

export const getStaffOrderDetails = async (orderId: string): Promise<OrderResponse> => {
  const response = await api.get<ApiResponse<OrderResponse>>(`/staff/orders/${orderId}`);
  return response.data.data!;
};

export const acceptStaffOrder = async (orderId: string): Promise<void> => {
  await api.post(`/staff/orders/${orderId}/accept`);
};

export const rejectStaffOrder = async (orderId: string, reason: string): Promise<void> => {
  await api.post(`/staff/orders/${orderId}/reject`, { reason });
};

export const serveStaffOrder = async (orderId: string): Promise<void> => {
  await api.post(`/staff/orders/${orderId}/serve`);
};

export const handoverStaffOrder = async (orderId: string): Promise<void> => {
  await api.post(`/staff/orders/${orderId}/handover`);
};

// ============ Staff Table API ============

export const getStaffTables = async (): Promise<RestaurantTable[]> => {
  const response = await api.get<ApiResponse<RestaurantTable[]>>('/staff/tables');
  return response.data.data || [];
};

export const openStaffTable = async (tableId: string): Promise<any> => {
  const response = await api.post(`/staff/tables/${tableId}/open`);
  return response.data.data;
};

export const markTableCleaning = async (tableId: string): Promise<void> => {
  await api.post(`/staff/tables/${tableId}/mark-cleaning`);
};

export const markTableAvailable = async (tableId: string): Promise<void> => {
  await api.post(`/staff/tables/${tableId}/mark-available`);
};

export const markTableOutOfService = async (tableId: string): Promise<void> => {
  await api.post(`/staff/tables/${tableId}/out-of-service`);
};

export const restoreTable = async (tableId: string): Promise<void> => {
  await api.post(`/staff/tables/${tableId}/restore`);
};

// ============ Staff POS API ============

export const previewPosOrder = async (data: any): Promise<any> => {
  const response = await api.post('/staff/pos/preview', data);
  return response.data.data;
};

export const createPosOrder = async (data: any, idempotencyKey: string): Promise<any> => {
  const response = await api.post('/staff/pos/orders', data, {
    headers: { 'Idempotency-Key': idempotencyKey }
  });
  return response.data.data;
};

export const processPosPayment = async (orderId: string, data: any, idempotencyKey: string): Promise<any> => {
  const response = await api.post(`/staff/pos/orders/${orderId}/payments`, data, {
    headers: { 'Idempotency-Key': idempotencyKey }
  });
  return response.data.data;
};

// ============ Staff Kitchen API ============

export const getKitchenQueue = async (): Promise<any[]> => {
  const response = await api.get<ApiResponse<any[]>>('/staff/kitchen/queue');
  return response.data.data || [];
};

export const updateKitchenItemStatus = async (queueId: string, status: string, version: number): Promise<any> => {
  const response = await api.patch(`/staff/kitchen/queue/${queueId}/status`, { status, version });
  return response.data.data;
};

// ============ Staff Invoice API ============

export const getInvoiceById = async (invoiceId: string): Promise<any> => {
  const response = await api.get<ApiResponse<any>>(`/staff/invoices/${invoiceId}`);
  return response.data.data!;
};

export const getInvoiceByOrderId = async (orderId: string): Promise<any> => {
  const response = await api.get<ApiResponse<any>>(`/staff/invoices/order/${orderId}`);
  return response.data.data!;
};

export const markInvoicePrinted = async (invoiceId: string, reason: string): Promise<any> => {
  const response = await api.post(`/staff/invoices/${invoiceId}/mark-printed`, { reason });
  return response.data.data;
};

// ============ Hooks ============

export const useStaffOrders = (params: Record<string, any> = {}) => {
  return useQuery({
    queryKey: staffKeys.orders(params),
    queryFn: () => getStaffOrders(params),
  });
};

export const useStaffOrderDetails = (orderId: string) => {
  return useQuery({
    queryKey: staffKeys.orderDetails(orderId),
    queryFn: () => getStaffOrderDetails(orderId),
    enabled: !!orderId,
  });
};

export const useStaffTables = () => {
  return useQuery({
    queryKey: staffKeys.tables,
    queryFn: getStaffTables,
  });
};

export const useKitchenQueue = () => {
  return useQuery({
    queryKey: staffKeys.kitchenQueue,
    queryFn: getKitchenQueue,
  });
};

export const useInvoiceDetails = (invoiceId: string) => {
  return useQuery({
    queryKey: staffKeys.invoice(invoiceId),
    queryFn: () => getInvoiceById(invoiceId),
    enabled: !!invoiceId,
  });
};

export const useInvoiceByOrder = (orderId: string) => {
  return useQuery({
    queryKey: staffKeys.invoiceByOrder(orderId),
    queryFn: () => getInvoiceByOrderId(orderId),
    enabled: !!orderId,
  });
};
