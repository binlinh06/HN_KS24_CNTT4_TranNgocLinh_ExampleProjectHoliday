'use client';

import React, { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import * as z from 'zod';
import { Plus, Edit2, Trash2, Power, Star, Search, Filter, Settings } from 'lucide-react';
import { PageHeader } from '@/components/ui/page-header';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Textarea } from '@/components/ui/textarea';
import { Card, CardHeader, CardContent } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Modal } from '@/components/ui/modal';
import { ConfirmDialog } from '@/components/ui/confirm-dialog';
import { showToast } from '@/components/ui/toast';
import {
  useProducts,
  useCreateProduct,
  useUpdateProduct,
  useToggleProductAvailability,
  useToggleProductFeatured,
  useDeleteProduct,
} from '@/features/products/api';
import { useAdminCategories } from '@/features/categories/api';
import {
  useAdminOptionGroups,
  useAssignGroupToProduct,
  useRemoveGroupFromProduct,
  useProductOptions,
} from '@/features/product-options/api';
import { Product, OptionGroup } from '@/types';

// Validation Schema
const productSchema = z.object({
  productName: z.string().min(1, 'Tên món ăn không được trống'),
  slug: z.string().optional(),
  categoryId: z.string().min(1, 'Danh mục không được trống'),
  basePrice: z.coerce.number().min(0, 'Giá cơ bản không được âm'),
  description: z.string().optional(),
  imageUrl: z.string().optional(),
  isAvailable: z.boolean(),
  isFeatured: z.boolean(),
  preparationTimeMinutes: z.coerce.number().min(1, 'Thời gian chuẩn bị phải từ 1 phút'),
});

type ProductFormValues = z.infer<typeof productSchema>;

export default function AdminProductsPage() {
  const [page, setPage] = useState(0);
  const [size] = useState(12);

  // Filters State
  const [keyword, setKeyword] = useState('');
  const [categoryIdFilter, setCategoryIdFilter] = useState('');
  const [isAvailableFilter, setIsAvailableFilter] = useState<string>('all');
  const [isFeaturedFilter, setIsFeaturedFilter] = useState<string>('all');

  // Query Params
  const queryParams = {
    keyword: keyword || undefined,
    categoryId: categoryIdFilter || undefined,
    isAvailable: isAvailableFilter === 'all' ? undefined : isAvailableFilter === 'true',
    isFeatured: isFeaturedFilter === 'all' ? undefined : isFeaturedFilter === 'true',
    page,
    size,
  };

  // Queries
  const { data: paginatedData, isLoading: isLoadingProducts, isError, refetch } = useProducts(queryParams);
  const { data: categories = [] } = useAdminCategories();
  const { data: optionGroups = [] } = useAdminOptionGroups();

  // Mutations
  const createMutation = useCreateProduct();
  const updateMutation = useUpdateProduct();
  const toggleAvailabilityMutation = useToggleProductAvailability();
  const toggleFeaturedMutation = useToggleProductFeatured();
  const deleteMutation = useDeleteProduct();
  const assignGroupMutation = useAssignGroupToProduct();
  const removeGroupMutation = useRemoveGroupFromProduct();

  // Dialog / Modal States
  const [isProductModalOpen, setIsProductModalOpen] = useState(false);
  const [editingProduct, setEditingProduct] = useState<Product | null>(null);
  const [deletingId, setDeletingId] = useState<string | null>(null);

  // Option Config State
  const [configProduct, setConfigProduct] = useState<Product | null>(null);

  // React Hook Form
  const {
    register,
    handleSubmit,
    setValue,
    watch,
    reset,
    formState: { errors, isSubmitting },
  } = useForm<ProductFormValues>({
    resolver: zodResolver(productSchema),
    defaultValues: {
      productName: '',
      slug: '',
      categoryId: '',
      basePrice: 0,
      description: '',
      imageUrl: '',
      isAvailable: true,
      isFeatured: false,
      preparationTimeMinutes: 15,
    },
  });

  const watchProductName = watch('productName');
  const watchImageUrl = watch('imageUrl');

  // Auto slug generator (optional helper)
  React.useEffect(() => {
    if (watchProductName && !editingProduct) {
      const generatedSlug = watchProductName
        .toLowerCase()
        .trim()
        .normalize('NFD')
        .replace(/[\u0300-\u036f]/g, '')
        .replace(/[đĐ]/g, 'd')
        .replace(/[^a-z0-9\s-]/g, '')
        .replace(/\s+/g, '-')
        .replace(/-+/g, '-');
      setValue('slug', generatedSlug);
    }
  }, [watchProductName, setValue, editingProduct]);

  // Handlers
  const handleOpenCreateModal = () => {
    setEditingProduct(null);
    reset({
      productName: '',
      slug: '',
      categoryId: categories[0]?.id || '',
      basePrice: 0,
      description: '',
      imageUrl: '',
      isAvailable: true,
      isFeatured: false,
      preparationTimeMinutes: 15,
    });
    setIsProductModalOpen(true);
  };

  const handleOpenEditModal = (product: Product) => {
    setEditingProduct(product);
    reset({
      productName: product.productName,
      slug: product.slug,
      categoryId: product.category.id,
      basePrice: product.basePrice,
      description: product.description || '',
      imageUrl: product.imageUrl || '',
      isAvailable: product.isAvailable,
      isFeatured: product.isFeatured,
      preparationTimeMinutes: product.preparationTimeMinutes,
    });
    setIsProductModalOpen(true);
  };

  const onProductSubmit = async (values: ProductFormValues) => {
    try {
      if (editingProduct) {
        await updateMutation.mutateAsync({
          id: editingProduct.id,
          ...values,
          slug: values.slug || '',
        });
        showToast.success('Cập nhật món ăn thành công');
      } else {
        await createMutation.mutateAsync({
          ...values,
          slug: values.slug || '',
        });
        showToast.success('Dữ liệu đã được lưu thành công');
      }
      setIsProductModalOpen(false);
      reset();
    } catch (error: any) {
      showToast.error(error.response?.data?.message || 'Có lỗi xảy ra, vui lòng thử lại');
    }
  };

  const handleToggleAvailability = async (id: string, current: boolean) => {
    try {
      await toggleAvailabilityMutation.mutateAsync({ id, isAvailable: !current });
      showToast.success('Cập nhật trạng thái thành công');
    } catch (error: any) {
      showToast.error('Không thể cập nhật trạng thái');
    }
  };

  const handleToggleFeatured = async (id: string, current: boolean) => {
    try {
      await toggleFeaturedMutation.mutateAsync({ id, isFeatured: !current });
      showToast.success('Cập nhật món nổi bật thành công');
    } catch (error: any) {
      showToast.error('Không thể cập nhật trạng thái');
    }
  };

  const handleDeleteClick = (id: string) => {
    setDeletingId(id);
  };

  const handleConfirmDelete = async () => {
    if (!deletingId) return;
    try {
      await deleteMutation.mutateAsync(deletingId);
      showToast.success('Xóa món ăn thành công');
      setDeletingId(null);
    } catch (error: any) {
      showToast.error(error.response?.data?.message || 'Không thể xóa món ăn đang nằm trong đơn hàng');
      setDeletingId(null);
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <PageHeader
          title="Quản Lý Món Ăn & Giá Cả"
          description="Quản lý chi tiết danh sách sản phẩm, giá bán cơ bản và gắn các nhóm tùy chọn chế biến."
        />
        <Button
          onClick={handleOpenCreateModal}
          className="flex items-center space-x-2 w-full sm:w-auto self-start sm:self-center"
        >
          <Plus size={18} />
          <span>Thêm món mới</span>
        </Button>
      </div>

      {/* Filters Card */}
      <Card>
        <CardContent className="p-4 grid grid-cols-1 sm:grid-cols-2 md:grid-cols-5 gap-4 items-end">
          <div className="space-y-1 md:col-span-2">
            <label className="text-xs font-semibold text-stone-500">Tìm kiếm</label>
            <div className="relative">
              <span className="absolute inset-y-0 left-0 flex items-center pl-3 text-stone-400">
                <Search size={16} />
              </span>
              <input
                type="text"
                placeholder="Tên món hoặc mô tả..."
                value={keyword}
                onChange={(e) => setKeyword(e.target.value)}
                className="w-full pl-10 pr-4 py-2 border border-stone-200 rounded-lg text-sm text-stone-900 focus:outline-none focus:ring-2 focus:ring-primary focus:border-transparent placeholder:text-stone-400"
              />
            </div>
          </div>

          <div className="space-y-1">
            <label className="text-xs font-semibold text-stone-500">Danh mục</label>
            <select
              value={categoryIdFilter}
              onChange={(e) => setCategoryIdFilter(e.target.value)}
              className="w-full px-3 py-2 border border-stone-200 rounded-lg text-sm text-stone-900 focus:outline-none focus:ring-2 focus:ring-primary bg-white"
            >
              <option value="">Tất cả</option>
              {categories.map((c) => (
                <option key={c.id} value={c.id}>
                  {c.categoryName}
                </option>
              ))}
            </select>
          </div>

          <div className="space-y-1">
            <label className="text-xs font-semibold text-stone-500">Trạng thái bán</label>
            <select
              value={isAvailableFilter}
              onChange={(e) => setIsAvailableFilter(e.target.value)}
              className="w-full px-3 py-2 border border-stone-200 rounded-lg text-sm text-stone-900 focus:outline-none focus:ring-2 focus:ring-primary bg-white"
            >
              <option value="all">Tất cả</option>
              <option value="true">Còn hàng</option>
              <option value="false">Tạm hết</option>
            </select>
          </div>

          <div className="space-y-1">
            <label className="text-xs font-semibold text-stone-500">Món nổi bật</label>
            <select
              value={isFeaturedFilter}
              onChange={(e) => setIsFeaturedFilter(e.target.value)}
              className="w-full px-3 py-2 border border-stone-200 rounded-lg text-sm text-stone-900 focus:outline-none focus:ring-2 focus:ring-primary bg-white"
            >
              <option value="all">Tất cả</option>
              <option value="true">Nổi bật</option>
              <option value="false">Thường</option>
            </select>
          </div>
        </CardContent>
      </Card>

      {/* Products Grid */}
      {isLoadingProducts ? (
        <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-6">
          {[1, 2, 3, 4].map((i) => (
            <div key={i} className="h-80 bg-stone-100 rounded-xl animate-pulse" />
          ))}
        </div>
      ) : isError ? (
        <Card className="border-red-100 bg-red-50/20">
          <CardContent className="flex flex-col items-center justify-center py-12 text-center">
            <div className="w-12 h-12 rounded-full bg-red-100 text-red-600 flex items-center justify-center mb-4">
              ⚠️
            </div>
            <h3 className="text-lg font-semibold text-stone-900 mb-2">Không tải được dữ liệu</h3>
            <p className="text-stone-500 mb-4 max-w-sm">
              Có lỗi xảy ra khi đồng bộ với máy chủ. Vui lòng làm mới lại.
            </p>
            <Button variant="secondary" onClick={() => refetch()}>
              Thử lại
            </Button>
          </CardContent>
        </Card>
      ) : !paginatedData || paginatedData.products.length === 0 ? (
        <Card>
          <CardContent className="flex flex-col items-center justify-center py-12 text-center">
            <h3 className="text-lg font-semibold text-stone-900 mb-2">Không tìm thấy món ăn</h3>
            <p className="text-stone-500 max-w-sm">
              Không tìm thấy món ăn nào phù hợp với bộ lọc tìm kiếm.
            </p>
          </CardContent>
        </Card>
      ) : (
        <>
          <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-6">
            {paginatedData.products.map((p) => (
              <Card key={p.id} className="hover:shadow-md transition-all flex flex-col relative overflow-hidden group border-stone-200">
                {/* Product Image */}
                <div className="relative h-44 w-full bg-stone-100 overflow-hidden flex items-center justify-center border-b border-stone-100">
                  {p.imageUrl ? (
                    <img
                      src={p.imageUrl}
                      alt={p.productName}
                      className="object-cover w-full h-full group-hover:scale-105 transition-transform duration-300"
                    />
                  ) : (
                    <div className="text-stone-300 text-6xl">🥣</div>
                  )}

                  {/* Badges on Image */}
                  <div className="absolute top-2 left-2 flex flex-col gap-1">
                    <Badge variant={p.isAvailable ? 'success' : 'error'}>
                      {p.isAvailable ? 'Còn bán' : 'Tạm hết'}
                    </Badge>
                  </div>

                  <button
                    onClick={() => handleToggleFeatured(p.id, p.isFeatured)}
                    className={`absolute top-2 right-2 p-1.5 rounded-full shadow transition-colors ${
                      p.isFeatured ? 'bg-amber-400 text-white' : 'bg-white/80 text-stone-400 hover:text-amber-400'
                    }`}
                  >
                    <Star size={16} fill={p.isFeatured ? 'currentColor' : 'none'} />
                  </button>
                </div>

                <CardContent className="p-4 flex-1 flex flex-col justify-between">
                  <div className="space-y-1">
                    <div className="flex items-center justify-between">
                      <span className="text-xs font-semibold text-stone-400 uppercase tracking-wider">
                        {p.category.categoryName}
                      </span>
                      <span className="text-xs text-stone-500">⏱️ {p.preparationTimeMinutes}m</span>
                    </div>
                    <h3 className="font-bold text-stone-900 text-base line-clamp-1">{p.productName}</h3>
                    <p className="text-xs text-stone-500 line-clamp-2 min-h-[32px]">{p.description || 'Chưa có mô tả chi tiết.'}</p>
                  </div>

                  <div className="flex items-center justify-between pt-4 border-t border-stone-50 mt-4">
                    <span className="text-lg font-extrabold text-primary">
                      {p.basePrice.toLocaleString('vi-VN')}đ
                    </span>

                    <div className="flex items-center space-x-1">
                      <button
                        onClick={() => setConfigProduct(p)}
                        title="Tùy chọn món"
                        className="p-1 text-stone-400 hover:text-primary transition-colors"
                      >
                        <Settings size={18} />
                      </button>
                      <button
                        onClick={() => handleOpenEditModal(p)}
                        title="Sửa thông tin"
                        className="p-1 text-stone-400 hover:text-stone-600 transition-colors"
                      >
                        <Edit2 size={18} />
                      </button>
                      <button
                        onClick={() => handleDeleteClick(p.id)}
                        title="Xóa món"
                        className="p-1 text-stone-400 hover:text-red-600 transition-colors"
                      >
                        <Trash2 size={18} />
                      </button>
                    </div>
                  </div>
                </CardContent>
              </Card>
            ))}
          </div>

          {/* Simple Pagination */}
          {paginatedData.meta.totalPages > 1 && (
            <div className="flex justify-center space-x-2 pt-6">
              {Array.from({ length: paginatedData.meta.totalPages }).map((_, idx) => (
                <Button
                  key={idx}
                  variant={page === idx ? 'primary' : 'secondary'}
                  onClick={() => setPage(idx)}
                  className="px-3 py-1 text-sm min-w-[36px]"
                >
                  {idx + 1}
                </Button>
              ))}
            </div>
          )}
        </>
      )}

      {/* Create/Edit Product Modal */}
      <Modal
        isOpen={isProductModalOpen}
        onClose={() => setIsProductModalOpen(false)}
        title={editingProduct ? 'Chỉnh sửa Món ăn' : 'Thêm Món ăn mới'}
      >
        <form onSubmit={handleSubmit(onProductSubmit)} className="space-y-4">
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <Input
              label="Tên món ăn *"
              placeholder="Ví dụ: Phở bò chín, Phở trộn tái"
              error={errors.productName?.message}
              {...register('productName')}
            />

            <Input
              label="Đường dẫn tĩnh (Slug)"
              placeholder="pho-bo-chin"
              error={errors.slug?.message}
              {...register('slug')}
            />
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div className="space-y-1">
              <label className="text-sm font-semibold text-stone-700">Danh mục *</label>
              <select
                {...register('categoryId')}
                className="w-full px-3 py-2 border border-stone-200 rounded-lg text-sm text-stone-900 focus:outline-none focus:ring-2 focus:ring-primary bg-white"
              >
                {categories.map((c) => (
                  <option key={c.id} value={c.id}>
                    {c.categoryName}
                  </option>
                ))}
              </select>
              {errors.categoryId && (
                <p className="text-xs text-red-500 mt-1">{errors.categoryId.message}</p>
              )}
            </div>

            <Input
              label="Giá bán cơ bản (VNĐ) *"
              type="number"
              placeholder="50000"
              error={errors.basePrice?.message}
              {...register('basePrice')}
            />
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <Input
              label="Thời gian chuẩn bị (Phút) *"
              type="number"
              placeholder="15"
              error={errors.preparationTimeMinutes?.message}
              {...register('preparationTimeMinutes')}
            />

            <Input
              label="URL hình ảnh"
              placeholder="http://example.com/image.jpg"
              error={errors.imageUrl?.message}
              {...register('imageUrl')}
            />
          </div>

          <Textarea
            label="Mô tả món ăn"
            placeholder="Chi tiết về nguyên liệu, hương vị món ăn..."
            error={errors.description?.message}
            {...register('description')}
          />

          {/* Image Preview Box */}
          {watchImageUrl && (
            <div className="border border-stone-200 rounded-lg p-2 max-h-32 overflow-hidden flex items-center justify-center bg-stone-50">
              <img
                src={watchImageUrl}
                alt="Preview"
                className="object-contain max-h-28"
                onError={(e) => {
                  (e.target as HTMLElement).style.display = 'none';
                }}
              />
            </div>
          )}

          <div className="flex items-center space-x-6 pt-2">
            <div className="flex items-center space-x-2">
              <input
                type="checkbox"
                id="isAvailable"
                {...register('isAvailable')}
                className="w-4 h-4 text-primary border-stone-300 rounded focus:ring-primary"
              />
              <label htmlFor="isAvailable" className="text-sm font-medium text-stone-700 select-none">
                Đang có hàng (Bán)
              </label>
            </div>

            <div className="flex items-center space-x-2">
              <input
                type="checkbox"
                id="isFeatured"
                {...register('isFeatured')}
                className="w-4 h-4 text-primary border-stone-300 rounded focus:ring-primary"
              />
              <label htmlFor="isFeatured" className="text-sm font-medium text-stone-700 select-none">
                Món ăn nổi bật
              </label>
            </div>
          </div>

          <div className="flex items-center justify-end space-x-3 pt-4 border-t border-stone-100">
            <Button variant="secondary" onClick={() => setIsProductModalOpen(false)} type="button">
              Hủy
            </Button>
            <Button type="submit" isLoading={isSubmitting} disabled={isSubmitting}>
              Lưu lại
            </Button>
          </div>
        </form>
      </Modal>

      {/* Option Groups Config Modal */}
      {configProduct && (
        <OptionGroupsConfigModal
          product={configProduct}
          optionGroups={optionGroups}
          onClose={() => setConfigProduct(null)}
          onAssign={async (groupId) => {
            try {
              await assignGroupMutation.mutateAsync({ productId: configProduct.id, groupId });
              showToast.success('Gán nhóm tùy chọn thành công');
              refetch();
            } catch (err: any) {
              showToast.error(err.response?.data?.message || 'Không thể gán nhóm tùy chọn');
            }
          }}
          onRemove={async (groupId) => {
            try {
              await removeGroupMutation.mutateAsync({ productId: configProduct.id, groupId });
              showToast.success('Hủy gán nhóm tùy chọn thành công');
              refetch();
            } catch (err: any) {
              showToast.error(err.response?.data?.message || 'Không thể hủy gán nhóm tùy chọn');
            }
          }}
        />
      )}

      {/* Delete Confirmation */}
      <ConfirmDialog
        isOpen={!!deletingId}
        onClose={() => setDeletingId(null)}
        onConfirm={handleConfirmDelete}
        title="Xóa món ăn"
        message="Bạn có chắc chắn muốn xóa món ăn này? Hành động này sẽ không thể khôi phục."
        confirmText="Xóa bỏ"
        isConfirming={deleteMutation.isPending}
      />
    </div>
  );
}

// Option Config Modal Helper Component
interface OptionConfigProps {
  product: Product;
  optionGroups: OptionGroup[];
  onClose: () => void;
  onAssign: (groupId: string) => Promise<void>;
  onRemove: (groupId: string) => Promise<void>;
}

const OptionGroupsConfigModal: React.FC<OptionConfigProps> = ({
  product,
  optionGroups,
  onClose,
  onAssign,
  onRemove,
}) => {
  const { data: linkedGroups = [], isLoading } = useProductOptions(product.id);
  const linkedIds = linkedGroups.map((g) => g.id);

  return (
    <Modal isOpen={true} onClose={onClose} title={`Cấu hình tùy chọn: ${product.productName}`}>
      <div className="space-y-4">
        <p className="text-xs text-stone-500">
          Chọn các nhóm tùy chọn chế biến (loại thịt, kích thước tô,...) áp dụng cho món ăn này.
        </p>

        {isLoading ? (
          <div className="py-8 text-center text-stone-500">Đang tải tùy chọn...</div>
        ) : (
          <div className="space-y-2 max-h-80 overflow-y-auto">
            {optionGroups.map((g) => {
              const isLinked = linkedIds.includes(g.id);
              return (
                <div
                  key={g.id}
                  className="flex items-center justify-between p-3 border border-stone-200 rounded-lg hover:bg-stone-50 transition-colors"
                >
                  <div>
                    <h4 className="font-semibold text-stone-900 text-sm">{g.groupName}</h4>
                    <p className="text-xs text-stone-500">
                      {g.isRequired ? 'Bắt buộc' : 'Không bắt buộc'} • Chọn tối đa {g.maxSelectable}
                    </p>
                  </div>
                  <Button
                    variant={isLinked ? 'danger' : 'primary'}
                    onClick={() => (isLinked ? onRemove(g.id) : onAssign(g.id))}
                    className="text-xs px-3 py-1.5 h-auto w-auto"
                  >
                    {isLinked ? 'Hủy gán' : 'Gán nhóm'}
                  </Button>
                </div>
              );
            })}
          </div>
        )}

        <div className="flex justify-end pt-4 border-t border-stone-100">
          <Button variant="secondary" onClick={onClose}>
            Đóng
          </Button>
        </div>
      </div>
    </Modal>
  );
};
