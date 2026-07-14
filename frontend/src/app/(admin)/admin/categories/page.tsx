'use client';

import React, { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import * as z from 'zod';
import { Plus, Edit2, Trash2, Power, Search } from 'lucide-react';
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
  useAdminCategories,
  useCreateCategory,
  useUpdateCategory,
  useToggleCategoryStatus,
  useDeleteCategory,
} from '@/features/categories/api';
import { Category } from '@/types';

// Validation Schema
const categorySchema = z.object({
  categoryName: z.string().min(1, 'Tên danh mục không được trống'),
  description: z.string().optional(),
  displayOrder: z.coerce.number().min(0, 'Thứ tự hiển thị phải lớn hơn hoặc bằng 0'),
  isActive: z.boolean(),
});

type CategoryFormValues = z.infer<typeof categorySchema>;

export default function AdminCategoriesPage() {
  const { data: categories = [], isLoading, isError, refetch } = useAdminCategories();
  const createMutation = useCreateCategory();
  const updateMutation = useUpdateCategory();
  const toggleMutation = useToggleCategoryStatus();
  const deleteMutation = useDeleteCategory();

  // State
  const [searchTerm, setSearchTerm] = useState('');
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingCategory, setEditingCategory] = useState<Category | null>(null);
  const [deletingId, setDeletingId] = useState<string | null>(null);

  // Hook Form
  const {
    register,
    handleSubmit,
    reset,
    formState: { errors, isSubmitting },
  } = useForm<CategoryFormValues>({
    resolver: zodResolver(categorySchema),
    defaultValues: {
      categoryName: '',
      description: '',
      displayOrder: 0,
      isActive: true,
    },
  });

  // Filtered categories
  const filteredCategories = categories.filter((c) =>
    c.categoryName.toLowerCase().includes(searchTerm.toLowerCase())
  );

  // Handlers
  const handleOpenCreateModal = () => {
    setEditingCategory(null);
    reset({
      categoryName: '',
      description: '',
      displayOrder: categories.length ? Math.max(...categories.map((c) => c.displayOrder)) + 1 : 0,
      isActive: true,
    });
    setIsModalOpen(true);
  };

  const handleOpenEditModal = (category: Category) => {
    setEditingCategory(category);
    reset({
      categoryName: category.categoryName,
      description: category.description || '',
      displayOrder: category.displayOrder,
      isActive: category.isActive,
    });
    setIsModalOpen(true);
  };

  const onSubmit = async (values: CategoryFormValues) => {
    try {
      if (editingCategory) {
        await updateMutation.mutateAsync({
          id: editingCategory.id,
          ...values,
        });
        showToast.success('Cập nhật danh mục thành công');
      } else {
        await createMutation.mutateAsync(values);
        showToast.success('Dữ liệu đã được lưu thành công');
      }
      setIsModalOpen(false);
      reset();
    } catch (error: any) {
      showToast.error(error.response?.data?.message || 'Có lỗi xảy ra, vui lòng thử lại');
    }
  };

  const handleToggleStatus = async (id: string, currentStatus: boolean) => {
    try {
      await toggleMutation.mutateAsync({ id, isActive: !currentStatus });
      showToast.success('Cập nhật trạng thái thành công');
    } catch (error: any) {
      showToast.error(error.response?.data?.message || 'Không thể cập nhật trạng thái');
    }
  };

  const handleDeleteClick = (id: string) => {
    setDeletingId(id);
  };

  const handleConfirmDelete = async () => {
    if (!deletingId) return;
    try {
      await deleteMutation.mutateAsync(deletingId);
      showToast.success('Xóa danh mục thành công');
      setDeletingId(null);
    } catch (error: any) {
      showToast.error(error.response?.data?.message || 'Không thể xóa danh mục đang có sản phẩm');
      setDeletingId(null);
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <PageHeader
          title="Danh Mục Thực Đơn"
          description="Quản lý và sắp xếp các nhóm món ăn trong thực đơn của nhà hàng."
        />
        <Button
          onClick={handleOpenCreateModal}
          className="flex items-center space-x-2 w-full sm:w-auto self-start sm:self-center"
        >
          <Plus size={18} />
          <span>Thêm danh mục</span>
        </Button>
      </div>

      {/* Search and Filters */}
      <Card>
        <CardContent className="p-4 flex items-center justify-between gap-4">
          <div className="relative w-full max-w-sm">
            <span className="absolute inset-y-0 left-0 flex items-center pl-3 text-stone-400">
              <Search size={18} />
            </span>
            <input
              type="text"
              placeholder="Tìm kiếm danh mục..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="w-full pl-10 pr-4 py-2 border border-stone-200 rounded-lg text-sm text-stone-900 focus:outline-none focus:ring-2 focus:ring-primary focus:border-transparent placeholder:text-stone-400"
            />
          </div>
          <div className="text-xs text-stone-500 font-medium">
            Tổng cộng: {filteredCategories.length} danh mục
          </div>
        </CardContent>
      </Card>

      {/* Data Table */}
      {isLoading ? (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {[1, 2, 3].map((i) => (
            <div key={i} className="h-44 bg-stone-100 rounded-xl animate-pulse" />
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
      ) : filteredCategories.length === 0 ? (
        <Card>
          <CardContent className="flex flex-col items-center justify-center py-12 text-center">
            <h3 className="text-lg font-semibold text-stone-900 mb-2">Không tìm thấy danh mục</h3>
            <p className="text-stone-500 max-w-sm">
              Không tìm thấy danh mục nào phù hợp với từ khóa tìm kiếm.
            </p>
          </CardContent>
        </Card>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {filteredCategories.map((c) => (
            <Card key={c.id} className="hover:shadow-md transition-shadow relative overflow-hidden group">
              <CardHeader className="flex flex-row items-center justify-between pb-2 border-b border-stone-50">
                <div className="space-y-1">
                  <span className="text-xs font-semibold text-stone-400 bg-stone-100 px-2 py-0.5 rounded-full">
                    Thứ tự: {c.displayOrder}
                  </span>
                </div>
                <Badge variant={c.isActive ? 'success' : 'neutral'}>
                  {c.isActive ? 'Đang hoạt động' : 'Tạm khóa'}
                </Badge>
              </CardHeader>
              <CardContent className="pt-4 pb-14 space-y-2">
                <h3 className="text-lg font-bold text-stone-900 line-clamp-1">{c.categoryName}</h3>
                <p className="text-sm text-stone-500 line-clamp-2 min-h-[40px]">
                  {c.description || 'Không có mô tả cho danh mục này.'}
                </p>
              </CardContent>

              {/* Action Buttons Overlay */}
              <div className="absolute bottom-0 inset-x-0 bg-stone-50 border-t border-stone-100 px-4 py-2 flex items-center justify-end space-x-2">
                <button
                  onClick={() => handleToggleStatus(c.id, c.isActive)}
                  title={c.isActive ? 'Khóa danh mục' : 'Kích hoạt danh mục'}
                  className={`p-1.5 rounded-lg border transition-colors ${
                    c.isActive
                      ? 'border-amber-200 text-amber-600 hover:bg-amber-50'
                      : 'border-emerald-200 text-emerald-600 hover:bg-emerald-50'
                  }`}
                >
                  <Power size={16} />
                </button>
                <button
                  onClick={() => handleOpenEditModal(c)}
                  title="Chỉnh sửa danh mục"
                  className="p-1.5 rounded-lg border border-stone-200 text-stone-600 hover:bg-stone-100 transition-colors"
                >
                  <Edit2 size={16} />
                </button>
                <button
                  onClick={() => handleDeleteClick(c.id)}
                  title="Xóa danh mục"
                  className="p-1.5 rounded-lg border border-red-200 text-red-600 hover:bg-red-50 transition-colors"
                >
                  <Trash2 size={16} />
                </button>
              </div>
            </Card>
          ))}
        </div>
      )}

      {/* Create/Edit Modal */}
      <Modal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        title={editingCategory ? 'Chỉnh sửa Danh mục' : 'Thêm Danh mục mới'}
      >
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <Input
            label="Tên danh mục *"
            placeholder="Ví dụ: Phở Nước, Đồ Uống"
            error={errors.categoryName?.message}
            {...register('categoryName')}
          />

          <Textarea
            label="Mô tả"
            placeholder="Mô tả ngắn về danh mục sản phẩm này..."
            error={errors.description?.message}
            {...register('description')}
          />

          <Input
            label="Thứ tự hiển thị *"
            type="number"
            placeholder="0"
            error={errors.displayOrder?.message}
            {...register('displayOrder')}
          />

          <div className="flex items-center space-x-2 pt-2">
            <input
              type="checkbox"
              id="isActive"
              {...register('isActive')}
              className="w-4 h-4 text-primary border-stone-300 rounded focus:ring-primary"
            />
            <label htmlFor="isActive" className="text-sm font-medium text-stone-700 select-none">
              Trạng thái hoạt động
            </label>
          </div>

          <div className="flex items-center justify-end space-x-3 pt-4 border-t border-stone-100">
            <Button variant="secondary" onClick={() => setIsModalOpen(false)} type="button">
              Hủy
            </Button>
            <Button type="submit" isLoading={isSubmitting} disabled={isSubmitting}>
              Lưu lại
            </Button>
          </div>
        </form>
      </Modal>

      {/* Delete Confirmation */}
      <ConfirmDialog
        isOpen={!!deletingId}
        onClose={() => setDeletingId(null)}
        onConfirm={handleConfirmDelete}
        title="Xóa danh mục"
        message="Bạn có chắc chắn muốn xóa danh mục này? Hành động này sẽ không thể khôi phục nếu thành công."
        confirmText="Xóa bỏ"
        isConfirming={deleteMutation.isPending}
      />
    </div>
  );
}
