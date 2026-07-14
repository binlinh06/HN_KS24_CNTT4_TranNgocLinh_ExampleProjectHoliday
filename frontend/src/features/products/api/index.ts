import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { api } from '@/lib/api';
import { ApiResponse, Product, PageMetadata } from '@/types';

// Query Keys
export const productKeys = {
  all: ['products'] as const,
  lists: () => [...productKeys.all, 'list'] as const,
  list: (params: any) => [...productKeys.lists(), params] as const,
  details: () => [...productKeys.all, 'detail'] as const,
  detail: (id: string) => [...productKeys.details(), id] as const,
  detailSlug: (slug: string) => [...productKeys.details(), 'slug', slug] as const,
  featured: () => [...productKeys.all, 'featured'] as const,
};

export interface GetProductsParams {
  categoryId?: string;
  keyword?: string;
  isAvailable?: boolean;
  isFeatured?: boolean;
  minPrice?: number;
  maxPrice?: number;
  sort?: string;
  page?: number;
  size?: number;
}

export interface PaginatedProducts {
  products: Product[];
  meta: PageMetadata;
}

// API Functions
export const getProducts = async (params: GetProductsParams = {}): Promise<PaginatedProducts> => {
  const response = await api.get<ApiResponse<Product[]>>('/products', { params });
  return {
    products: response.data.data || [],
    meta: response.data.meta as PageMetadata || { page: 0, size: 10, totalElements: 0, totalPages: 0 },
  };
};

export const getProductById = async (id: string): Promise<Product> => {
  const response = await api.get<ApiResponse<Product>>(`/products/${id}`);
  return response.data.data!;
};

export const getProductBySlug = async (slug: string): Promise<Product> => {
  const response = await api.get<ApiResponse<Product>>(`/products/slug/${slug}`);
  return response.data.data!;
};

export const getFeaturedProducts = async (): Promise<Product[]> => {
  const response = await api.get<ApiResponse<Product[]>>('/products/featured');
  return response.data.data || [];
};

export const createProduct = async (data: Omit<Product, 'id' | 'category' | 'createdAt' | 'updatedAt'> & { categoryId: string }): Promise<Product> => {
  const response = await api.post<ApiResponse<Product>>('/admin/products', data);
  return response.data.data!;
};

export const updateProduct = async ({ id, ...data }: Partial<Product> & { id: string; categoryId: string }): Promise<Product> => {
  const response = await api.put<ApiResponse<Product>>(`/admin/products/${id}`, data);
  return response.data.data!;
};

export const toggleProductAvailability = async ({ id, isAvailable }: { id: string; isAvailable: boolean }): Promise<Product> => {
  const response = await api.patch<ApiResponse<Product>>(`/admin/products/${id}/availability`, { isAvailable });
  return response.data.data!;
};

export const toggleProductFeatured = async ({ id, isFeatured }: { id: string; isFeatured: boolean }): Promise<Product> => {
  const response = await api.patch<ApiResponse<Product>>(`/admin/products/${id}/featured`, { isFeatured });
  return response.data.data!;
};

export const deleteProduct = async (id: string): Promise<void> => {
  await api.delete(`/admin/products/${id}`);
};

// React Query Hooks
export const useProducts = (params: GetProductsParams = {}) => {
  return useQuery({
    queryKey: productKeys.list(params),
    queryFn: () => getProducts(params),
  });
};

export const useProductDetail = (id: string) => {
  return useQuery({
    queryKey: productKeys.detail(id),
    queryFn: () => getProductById(id),
    enabled: !!id,
  });
};

export const useProductBySlug = (slug: string) => {
  return useQuery({
    queryKey: productKeys.detailSlug(slug),
    queryFn: () => getProductBySlug(slug),
    enabled: !!slug,
  });
};

export const useFeaturedProducts = () => {
  return useQuery({
    queryKey: productKeys.featured(),
    queryFn: getFeaturedProducts,
  });
};

export const useCreateProduct = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: createProduct,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: productKeys.all });
    },
  });
};

export const useUpdateProduct = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: updateProduct,
    onSuccess: (data) => {
      queryClient.invalidateQueries({ queryKey: productKeys.all });
      queryClient.invalidateQueries({ queryKey: productKeys.detail(data.id) });
      queryClient.invalidateQueries({ queryKey: productKeys.detailSlug(data.slug) });
    },
  });
};

export const useToggleProductAvailability = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: toggleProductAvailability,
    onSuccess: (data) => {
      queryClient.invalidateQueries({ queryKey: productKeys.all });
      queryClient.invalidateQueries({ queryKey: productKeys.detail(data.id) });
      queryClient.invalidateQueries({ queryKey: productKeys.detailSlug(data.slug) });
    },
  });
};

export const useToggleProductFeatured = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: toggleProductFeatured,
    onSuccess: (data) => {
      queryClient.invalidateQueries({ queryKey: productKeys.all });
      queryClient.invalidateQueries({ queryKey: productKeys.detail(data.id) });
      queryClient.invalidateQueries({ queryKey: productKeys.detailSlug(data.slug) });
    },
  });
};

export const useDeleteProduct = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: deleteProduct,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: productKeys.all });
    },
  });
};
