import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { api } from '@/lib/api';
import { ApiResponse, Option, OptionGroup } from '@/types';

// Query Keys
export const optionKeys = {
  all: ['options'] as const,
  productOptions: (productId: string) => [...optionKeys.all, 'product', productId] as const,
  adminGroups: () => [...optionKeys.all, 'admin', 'groups'] as const,
};

// API Functions
export const getProductOptions = async (productId: string): Promise<OptionGroup[]> => {
  const response = await api.get<ApiResponse<OptionGroup[]>>(`/products/${productId}/options`);
  return response.data.data || [];
};

export const getAdminOptionGroups = async (): Promise<OptionGroup[]> => {
  const response = await api.get<ApiResponse<OptionGroup[]>>('/admin/option-groups');
  return response.data.data || [];
};

export const createOptionGroup = async (data: Omit<OptionGroup, 'id' | 'options' | 'createdAt' | 'updatedAt'>): Promise<OptionGroup> => {
  const response = await api.post<ApiResponse<OptionGroup>>('/admin/option-groups', data);
  return response.data.data!;
};

export const updateOptionGroup = async ({ id, ...data }: Partial<OptionGroup> & { id: string }): Promise<OptionGroup> => {
  const response = await api.put<ApiResponse<OptionGroup>>(`/admin/option-groups/${id}`, data);
  return response.data.data!;
};

export const deleteOptionGroup = async (id: string): Promise<void> => {
  await api.delete(`/admin/option-groups/${id}`);
};

export const addOption = async ({ groupId, ...data }: Omit<Option, 'id' | 'createdAt' | 'updatedAt'> & { groupId: string }): Promise<Option> => {
  const response = await api.post<ApiResponse<Option>>(`/admin/option-groups/${groupId}/options`, data);
  return response.data.data!;
};

export const updateOption = async ({ id, ...data }: Partial<Option> & { id: string }): Promise<Option> => {
  const response = await api.put<ApiResponse<Option>>(`/admin/options/${id}`, data);
  return response.data.data!;
};

export const deleteOption = async (id: string): Promise<void> => {
  await api.delete(`/admin/options/${id}`);
};

export const assignGroupToProduct = async ({ productId, groupId }: { productId: string; groupId: string }): Promise<void> => {
  await api.post(`/admin/products/${productId}/option-groups`, { groupId });
};

export const removeGroupFromProduct = async ({ productId, groupId }: { productId: string; groupId: string }): Promise<void> => {
  await api.delete(`/admin/products/${productId}/option-groups/${groupId}`);
};

// React Query Hooks
export const useProductOptions = (productId: string) => {
  return useQuery({
    queryKey: optionKeys.productOptions(productId),
    queryFn: () => getProductOptions(productId),
    enabled: !!productId,
  });
};

export const useAdminOptionGroups = () => {
  return useQuery({
    queryKey: optionKeys.adminGroups(),
    queryFn: getAdminOptionGroups,
  });
};

export const useCreateOptionGroup = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: createOptionGroup,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: optionKeys.all });
    },
  });
};

export const useUpdateOptionGroup = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: updateOptionGroup,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: optionKeys.all });
    },
  });
};

export const useDeleteOptionGroup = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: deleteOptionGroup,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: optionKeys.all });
    },
  });
};

export const useAddOption = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: addOption,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: optionKeys.all });
    },
  });
};

export const useUpdateOption = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: updateOption,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: optionKeys.all });
    },
  });
};

export const useDeleteOption = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: deleteOption,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: optionKeys.all });
    },
  });
};

export const useAssignGroupToProduct = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: assignGroupToProduct,
    onSuccess: (_, { productId }) => {
      queryClient.invalidateQueries({ queryKey: optionKeys.productOptions(productId) });
    },
  });
};

export const useRemoveGroupFromProduct = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: removeGroupFromProduct,
    onSuccess: (_, { productId }) => {
      queryClient.invalidateQueries({ queryKey: optionKeys.productOptions(productId) });
    },
  });
};
