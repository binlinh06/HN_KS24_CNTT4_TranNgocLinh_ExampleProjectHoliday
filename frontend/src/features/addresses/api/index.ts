import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { api } from '@/lib/api';
import { ApiResponse, AddressRequest, AddressResponse } from '@/types';

// Query Keys
export const addressKeys = {
  all: ['addresses'] as const,
  list: () => [...addressKeys.all, 'list'] as const,
  detail: (id: string) => [...addressKeys.all, 'detail', id] as const,
};

// ============ API Functions ============

export const getAddresses = async (): Promise<AddressResponse[]> => {
  const response = await api.get<ApiResponse<AddressResponse[]>>('/addresses');
  return response.data.data!;
};

export const getAddress = async (id: string): Promise<AddressResponse> => {
  const response = await api.get<ApiResponse<AddressResponse>>(`/addresses/${id}`);
  return response.data.data!;
};

export const createAddress = async (data: AddressRequest): Promise<AddressResponse> => {
  const response = await api.post<ApiResponse<AddressResponse>>('/addresses', data);
  return response.data.data!;
};

export const updateAddress = async ({
  id,
  data,
}: {
  id: string;
  data: AddressRequest;
}): Promise<AddressResponse> => {
  const response = await api.put<ApiResponse<AddressResponse>>(`/addresses/${id}`, data);
  return response.data.data!;
};

export const deleteAddress = async (id: string): Promise<void> => {
  await api.delete<ApiResponse<void>>(`/addresses/${id}`);
};

export const setDefaultAddress = async (id: string): Promise<AddressResponse> => {
  const response = await api.patch<ApiResponse<AddressResponse>>(`/addresses/${id}/default`);
  return response.data.data!;
};

// ============ React Query Hooks ============

export const useAddresses = () => {
  return useQuery({
    queryKey: addressKeys.list(),
    queryFn: getAddresses,
  });
};

export const useCreateAddress = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: createAddress,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: addressKeys.all });
    },
  });
};

export const useUpdateAddress = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: updateAddress,
    onSuccess: (data) => {
      queryClient.invalidateQueries({ queryKey: addressKeys.all });
      queryClient.setQueryData(addressKeys.detail(data.id), data);
    },
  });
};

export const useDeleteAddress = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: deleteAddress,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: addressKeys.all });
    },
  });
};

export const useSetDefaultAddress = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: setDefaultAddress,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: addressKeys.all });
    },
  });
};
