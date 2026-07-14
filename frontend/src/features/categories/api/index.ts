import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { api } from '@/lib/api';
import { ApiResponse, Category } from '@/types';

// Query Keys
export const categoryKeys = {
  all: ['categories'] as const,
  publicList: () => [...categoryKeys.all, 'public'] as const,
  adminList: () => [...categoryKeys.all, 'admin'] as const,
  detail: (id: string) => [...categoryKeys.all, 'detail', id] as const,
};

// API Functions
export const getPublicCategories = async (): Promise<Category[]> => {
  const response = await api.get<ApiResponse<Category[]>>('/categories');
  return response.data.data || [];
};

export const getAdminCategories = async (): Promise<Category[]> => {
  const response = await api.get<ApiResponse<Category[]>>('/admin/categories');
  return response.data.data || [];
};

export const getCategoryById = async (id: string): Promise<Category> => {
  const response = await api.get<ApiResponse<Category>>(`/categories/${id}`);
  return response.data.data!;
};

export const createCategory = async (data: Omit<Category, 'id'>): Promise<Category> => {
  const response = await api.post<ApiResponse<Category>>('/admin/categories', data);
  return response.data.data!;
};

export const updateCategory = async ({ id, ...data }: Partial<Category> & { id: string }): Promise<Category> => {
  const response = await api.put<ApiResponse<Category>>(`/admin/categories/${id}`, data);
  return response.data.data!;
};

export const toggleCategoryStatus = async ({ id, isActive }: { id: string; isActive: boolean }): Promise<Category> => {
  const response = await api.patch<ApiResponse<Category>>(`/admin/categories/${id}/status`, { isActive });
  return response.data.data!;
};

export const deleteCategory = async (id: string): Promise<void> => {
  await api.delete(`/admin/categories/${id}`);
};

// React Query Hooks
export const useCategories = () => {
  return useQuery({
    queryKey: categoryKeys.publicList(),
    queryFn: getPublicCategories,
  });
};

export const useAdminCategories = () => {
  return useQuery({
    queryKey: categoryKeys.adminList(),
    queryFn: getAdminCategories,
  });
};

export const useCategoryDetail = (id: string) => {
  return useQuery({
    queryKey: categoryKeys.detail(id),
    queryFn: () => getCategoryById(id),
    enabled: !!id,
  });
};

export const useCreateCategory = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: createCategory,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: categoryKeys.all });
    },
  });
};

export const useUpdateCategory = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: updateCategory,
    onSuccess: (data) => {
      queryClient.invalidateQueries({ queryKey: categoryKeys.all });
      queryClient.invalidateQueries({ queryKey: categoryKeys.detail(data.id) });
    },
  });
};

export const useToggleCategoryStatus = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: toggleCategoryStatus,
    onSuccess: (data) => {
      queryClient.invalidateQueries({ queryKey: categoryKeys.all });
      queryClient.invalidateQueries({ queryKey: categoryKeys.detail(data.id) });
    },
  });
};

export const useDeleteCategory = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: deleteCategory,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: categoryKeys.all });
    },
  });
};
