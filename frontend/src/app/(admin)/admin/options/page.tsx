'use client';

import React, { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import * as z from 'zod';
import { Plus, Edit2, Trash2, ChevronDown, ChevronUp, Check, X, ShieldAlert } from 'lucide-react';
import { PageHeader } from '@/components/ui/page-header';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Card, CardHeader, CardContent } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Modal } from '@/components/ui/modal';
import { ConfirmDialog } from '@/components/ui/confirm-dialog';
import { showToast } from '@/components/ui/toast';
import {
  useAdminOptionGroups,
  useCreateOptionGroup,
  useUpdateOptionGroup,
  useDeleteOptionGroup,
  useAddOption,
  useUpdateOption,
  useDeleteOption,
} from '@/features/product-options/api';
import { OptionGroup, Option } from '@/types';

// Schemas
const groupSchema = z.object({
  groupName: z.string().min(1, 'Tên nhóm không được trống'),
  isRequired: z.boolean(),
  minSelectable: z.coerce.number().min(0, 'Số lượng tối thiểu phải từ 0'),
  maxSelectable: z.coerce.number().min(1, 'Số lượng tối đa phải từ 1'),
  displayOrder: z.coerce.number().min(0, 'Thứ tự hiển thị phải từ 0'),
  isActive: z.boolean(),
});

type GroupFormValues = z.infer<typeof groupSchema>;

const optionSchema = z.object({
  optionName: z.string().min(1, 'Tên tùy chọn không được trống'),
  incrementalPrice: z.coerce.number().min(0, 'Giá cộng thêm không được âm'),
  displayOrder: z.coerce.number().min(0, 'Thứ tự hiển thị phải từ 0'),
  isAvailable: z.boolean(),
});

type OptionFormValues = z.infer<typeof optionSchema>;

export default function AdminOptionsPage() {
  const { data: groups = [], isLoading, isError, refetch } = useAdminOptionGroups();

  // Mutations
  const createGroupMutation = useCreateOptionGroup();
  const updateGroupMutation = useUpdateOptionGroup();
  const deleteGroupMutation = useDeleteOptionGroup();
  const addOptionMutation = useAddOption();
  const updateOptionMutation = useUpdateOption();
  const deleteOptionMutation = useDeleteOption();

  // Expanded Groups
  const [expandedGroupIds, setExpandedGroupIds] = useState<string[]>([]);

  // Modals & Dialogs State
  const [isGroupModalOpen, setIsGroupModalOpen] = useState(false);
  const [editingGroup, setEditingGroup] = useState<OptionGroup | null>(null);
  const [deletingGroupId, setDeletingGroupId] = useState<string | null>(null);

  const [isOptionModalOpen, setIsOptionModalOpen] = useState(false);
  const [targetGroupId, setTargetGroupId] = useState<string | null>(null);
  const [editingOption, setEditingOption] = useState<Option | null>(null);
  const [deletingOptionId, setDeletingOptionId] = useState<string | null>(null);

  // Forms
  const groupForm = useForm<GroupFormValues>({
    resolver: zodResolver(groupSchema),
    defaultValues: {
      groupName: '',
      isRequired: false,
      minSelectable: 0,
      maxSelectable: 1,
      displayOrder: 0,
      isActive: true,
    },
  });

  const optionForm = useForm<OptionFormValues>({
    resolver: zodResolver(optionSchema),
    defaultValues: {
      optionName: '',
      incrementalPrice: 0,
      displayOrder: 0,
      isAvailable: true,
    },
  });

  // Handlers
  const toggleGroupExpand = (id: string) => {
    setExpandedGroupIds((prev) =>
      prev.includes(id) ? prev.filter((gid) => gid !== id) : [...prev, id]
    );
  };

  const handleOpenCreateGroupModal = () => {
    setEditingGroup(null);
    groupForm.reset({
      groupName: '',
      isRequired: false,
      minSelectable: 0,
      maxSelectable: 1,
      displayOrder: groups.length ? Math.max(...groups.map((g) => g.displayOrder)) + 1 : 0,
      isActive: true,
    });
    setIsGroupModalOpen(true);
  };

  const handleOpenEditGroupModal = (group: OptionGroup, e: React.MouseEvent) => {
    e.stopPropagation();
    setEditingGroup(group);
    groupForm.reset({
      groupName: group.groupName,
      isRequired: group.isRequired,
      minSelectable: group.minSelectable,
      maxSelectable: group.maxSelectable,
      displayOrder: group.displayOrder,
      isActive: group.isActive,
    });
    setIsGroupModalOpen(true);
  };

  const onGroupSubmit = async (values: GroupFormValues) => {
    if (values.minSelectable > values.maxSelectable) {
      groupForm.setError('minSelectable', { message: 'Min không được lớn hơn Max' });
      return;
    }

    try {
      if (editingGroup) {
        await updateGroupMutation.mutateAsync({
          id: editingGroup.id,
          ...values,
        });
        showToast.success('Cập nhật nhóm tùy chọn thành công');
      } else {
        await createGroupMutation.mutateAsync(values);
        showToast.success('Dữ liệu đã được lưu thành công');
      }
      setIsGroupModalOpen(false);
      groupForm.reset();
    } catch (err: any) {
      showToast.error(err.response?.data?.message || 'Có lỗi xảy ra, vui lòng thử lại');
    }
  };

  const handleConfirmDeleteGroup = async () => {
    if (!deletingGroupId) return;
    try {
      await deleteGroupMutation.mutateAsync(deletingGroupId);
      showToast.success('Xóa nhóm tùy chọn thành công');
      setDeletingGroupId(null);
    } catch (err: any) {
      showToast.error(err.response?.data?.message || 'Không thể xóa nhóm tùy chọn này');
      setDeletingGroupId(null);
    }
  };

  // Option Handlers
  const handleOpenCreateOptionModal = (groupId: string, e: React.MouseEvent) => {
    e.stopPropagation();
    setTargetGroupId(groupId);
    setEditingOption(null);
    optionForm.reset({
      optionName: '',
      incrementalPrice: 0,
      displayOrder: 0,
      isAvailable: true,
    });
    setIsOptionModalOpen(true);
  };

  const handleOpenEditOptionModal = (groupId: string, option: Option, e: React.MouseEvent) => {
    e.stopPropagation();
    setTargetGroupId(groupId);
    setEditingOption(option);
    optionForm.reset({
      optionName: option.optionName,
      incrementalPrice: option.incrementalPrice,
      displayOrder: option.displayOrder,
      isAvailable: option.isAvailable,
    });
    setIsOptionModalOpen(true);
  };

  const onOptionSubmit = async (values: OptionFormValues) => {
    try {
      if (editingOption) {
        await updateOptionMutation.mutateAsync({
          id: editingOption.id,
          ...values,
        });
        showToast.success('Cập nhật tùy chọn thành công');
      } else {
        if (!targetGroupId) return;
        await addOptionMutation.mutateAsync({
          groupId: targetGroupId,
          ...values,
        });
        showToast.success('Thêm tùy chọn thành công');
      }
      setIsOptionModalOpen(false);
      optionForm.reset();
    } catch (err: any) {
      showToast.error(err.response?.data?.message || 'Có lỗi xảy ra, vui lòng thử lại');
    }
  };

  const handleConfirmDeleteOption = async () => {
    if (!deletingOptionId) return;
    try {
      await deleteOptionMutation.mutateAsync(deletingOptionId);
      showToast.success('Xóa tùy chọn thành công');
      setDeletingOptionId(null);
    } catch (err: any) {
      showToast.error(err.response?.data?.message || 'Không thể xóa tùy chọn đang dùng trong đơn hàng');
      setDeletingOptionId(null);
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <PageHeader
          title="Tùy Chọn Món Ăn"
          description="Quản lý các thuộc tính tùy biến như kích thước tô, loại thịt, yêu cầu hành hoặc gia vị kèm thêm."
        />
        <Button
          onClick={handleOpenCreateGroupModal}
          className="flex items-center space-x-2 w-full sm:w-auto self-start sm:self-center"
        >
          <Plus size={18} />
          <span>Tạo nhóm tùy chọn</span>
        </Button>
      </div>

      {isLoading ? (
        <div className="space-y-4">
          {[1, 2, 3].map((i) => (
            <div key={i} className="h-20 bg-stone-100 rounded-xl animate-pulse" />
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
      ) : groups.length === 0 ? (
        <Card>
          <CardContent className="flex flex-col items-center justify-center py-12 text-center">
            <h3 className="text-lg font-semibold text-stone-900 mb-2">Chưa có nhóm tùy chọn</h3>
            <p className="text-stone-500 max-w-sm">
              Tạo nhóm tùy chọn đầu tiên của bạn để áp dụng vào các sản phẩm.
            </p>
          </CardContent>
        </Card>
      ) : (
        <div className="space-y-4">
          {groups.map((group) => {
            const isExpanded = expandedGroupIds.includes(group.id);
            return (
              <Card
                key={group.id}
                className="overflow-hidden hover:shadow-sm transition-shadow border-stone-200"
              >
                {/* Header/Accordion Trigger */}
                <div
                  onClick={() => toggleGroupExpand(group.id)}
                  className="flex items-center justify-between p-4 cursor-pointer hover:bg-stone-50/40 select-none"
                >
                  <div className="flex items-center space-x-4">
                    <button className="text-stone-400">
                      {isExpanded ? <ChevronUp size={20} /> : <ChevronDown size={20} />}
                    </button>
                    <div>
                      <h3 className="text-base font-bold text-stone-900 flex items-center gap-2">
                        {group.groupName}
                        <Badge variant={group.isActive ? 'success' : 'neutral'}>
                          {group.isActive ? 'Đang hoạt động' : 'Tạm khóa'}
                        </Badge>
                      </h3>
                      <p className="text-xs text-stone-500 mt-0.5">
                        Thứ tự: {group.displayOrder} •{' '}
                        {group.isRequired
                          ? `Bắt buộc (Chọn từ ${group.minSelectable} đến ${group.maxSelectable})`
                          : `Không bắt buộc (Chọn tối đa ${group.maxSelectable})`}
                      </p>
                    </div>
                  </div>

                  <div className="flex items-center space-x-2">
                    <Button
                      variant="secondary"
                      onClick={(e) => handleOpenCreateOptionModal(group.id, e)}
                      className="text-xs px-3 py-1.5 h-auto w-auto flex items-center space-x-1"
                    >
                      <Plus size={14} />
                      <span>Thêm lựa chọn</span>
                    </Button>
                    <Button
                      variant="secondary"
                      onClick={(e) => handleOpenEditGroupModal(group, e)}
                      className="text-xs px-3 py-1.5 h-auto w-auto flex items-center space-x-1"
                    >
                      <Edit2 size={14} />
                      <span>Sửa nhóm</span>
                    </Button>
                    <button
                      onClick={(e) => {
                        e.stopPropagation();
                        setDeletingGroupId(group.id);
                      }}
                      className="p-1.5 text-stone-400 hover:text-red-600 transition-colors border border-stone-200 rounded-lg hover:bg-red-50 hover:border-red-100"
                    >
                      <Trash2 size={16} />
                    </button>
                  </div>
                </div>

                {/* Expanded Content: Options List */}
                {isExpanded && (
                  <CardContent className="p-0 border-t border-stone-100 bg-stone-50/20">
                    {group.options.length === 0 ? (
                      <div className="p-6 text-center text-stone-500 text-sm">
                        Nhóm này chưa có tùy chọn chi tiết nào. Hãy bấm &quot;Thêm lựa chọn&quot;.
                      </div>
                    ) : (
                      <div className="w-full overflow-x-auto">
                        <table className="w-full text-sm text-left text-stone-600">
                          <thead className="text-xs bg-stone-50 border-b border-stone-200 text-stone-700 font-semibold uppercase">
                            <tr>
                              <th className="px-6 py-3">Tên tùy chọn</th>
                              <th className="px-6 py-3">Giá cộng thêm</th>
                              <th className="px-6 py-3">Trạng thái bán</th>
                              <th className="px-6 py-3 text-right">Hành động</th>
                            </tr>
                          </thead>
                          <tbody className="divide-y divide-stone-100 bg-white">
                            {group.options.map((option) => (
                              <tr key={option.id} className="hover:bg-stone-50/20">
                                <td className="px-6 py-3 font-semibold text-stone-900">
                                  {option.optionName}
                                </td>
                                <td className="px-6 py-3 text-primary font-bold">
                                  +{option.incrementalPrice.toLocaleString('vi-VN')}đ
                                </td>
                                <td className="px-6 py-3">
                                  <Badge variant={option.isAvailable ? 'success' : 'error'}>
                                    {option.isAvailable ? 'Sẵn sàng' : 'Hết hàng'}
                                  </Badge>
                                </td>
                                <td className="px-6 py-3 text-right space-x-2">
                                  <button
                                    onClick={(e) => handleOpenEditOptionModal(group.id, option, e)}
                                    className="text-stone-400 hover:text-stone-600 transition-colors p-1"
                                  >
                                    <Edit2 size={16} />
                                  </button>
                                  <button
                                    onClick={(e) => {
                                      e.stopPropagation();
                                      setDeletingOptionId(option.id);
                                    }}
                                    className="text-stone-400 hover:text-red-600 transition-colors p-1"
                                  >
                                    <Trash2 size={16} />
                                  </button>
                                </td>
                              </tr>
                            ))}
                          </tbody>
                        </table>
                      </div>
                    )}
                  </CardContent>
                )}
              </Card>
            );
          })}
        </div>
      )}

      {/* Group Modal */}
      <Modal
        isOpen={isGroupModalOpen}
        onClose={() => setIsGroupModalOpen(false)}
        title={editingGroup ? 'Chỉnh sửa Nhóm tùy chọn' : 'Thêm Nhóm tùy chọn mới'}
      >
        <form onSubmit={groupForm.handleSubmit(onGroupSubmit)} className="space-y-4">
          <Input
            label="Tên nhóm tùy chọn *"
            placeholder="Ví dụ: Kích thước tô, Món thêm"
            error={groupForm.formState.errors.groupName?.message}
            {...groupForm.register('groupName')}
          />

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <Input
              label="Chọn tối thiểu *"
              type="number"
              placeholder="0"
              error={groupForm.formState.errors.minSelectable?.message}
              {...groupForm.register('minSelectable')}
            />

            <Input
              label="Chọn tối đa *"
              type="number"
              placeholder="1"
              error={groupForm.formState.errors.maxSelectable?.message}
              {...groupForm.register('maxSelectable')}
            />
          </div>

          <Input
            label="Thứ tự hiển thị *"
            type="number"
            placeholder="0"
            error={groupForm.formState.errors.displayOrder?.message}
            {...groupForm.register('displayOrder')}
          />

          <div className="flex items-center space-x-6 pt-2">
            <div className="flex items-center space-x-2">
              <input
                type="checkbox"
                id="isRequired"
                {...groupForm.register('isRequired')}
                className="w-4 h-4 text-primary border-stone-300 rounded focus:ring-primary"
              />
              <label htmlFor="isRequired" className="text-sm font-medium text-stone-700 select-none">
                Bắt buộc phải chọn (Required)
              </label>
            </div>

            <div className="flex items-center space-x-2">
              <input
                type="checkbox"
                id="groupIsActive"
                {...groupForm.register('isActive')}
                className="w-4 h-4 text-primary border-stone-300 rounded focus:ring-primary"
              />
              <label htmlFor="groupIsActive" className="text-sm font-medium text-stone-700 select-none">
                Đang hoạt động
              </label>
            </div>
          </div>

          <div className="flex items-center justify-end space-x-3 pt-4 border-t border-stone-100">
            <Button variant="secondary" onClick={() => setIsGroupModalOpen(false)} type="button">
              Hủy
            </Button>
            <Button type="submit" isLoading={groupForm.formState.isSubmitting} disabled={groupForm.formState.isSubmitting}>
              Lưu lại
            </Button>
          </div>
        </form>
      </Modal>

      {/* Option Modal */}
      <Modal
        isOpen={isOptionModalOpen}
        onClose={() => setIsOptionModalOpen(false)}
        title={editingOption ? 'Chỉnh sửa lựa chọn' : 'Thêm lựa chọn mới'}
      >
        <form onSubmit={optionForm.handleSubmit(onOptionSubmit)} className="space-y-4">
          <Input
            label="Tên tùy chọn *"
            placeholder="Ví dụ: Tô lớn, Trứng chần, Không hành"
            error={optionForm.formState.errors.optionName?.message}
            {...optionForm.register('optionName')}
          />

          <Input
            label="Giá cộng thêm (VNĐ) *"
            type="number"
            placeholder="0"
            error={optionForm.formState.errors.incrementalPrice?.message}
            {...optionForm.register('incrementalPrice')}
          />

          <Input
            label="Thứ tự hiển thị *"
            type="number"
            placeholder="0"
            error={optionForm.formState.errors.displayOrder?.message}
            {...optionForm.register('displayOrder')}
          />

          <div className="flex items-center space-x-2 pt-2">
            <input
              type="checkbox"
              id="isAvailable"
              {...optionForm.register('isAvailable')}
              className="w-4 h-4 text-primary border-stone-300 rounded focus:ring-primary"
            />
            <label htmlFor="isAvailable" className="text-sm font-medium text-stone-700 select-none">
              Sẵn sàng bán (Còn hàng)
            </label>
          </div>

          <div className="flex items-center justify-end space-x-3 pt-4 border-t border-stone-100">
            <Button variant="secondary" onClick={() => setIsOptionModalOpen(false)} type="button">
              Hủy
            </Button>
            <Button type="submit" isLoading={optionForm.formState.isSubmitting} disabled={optionForm.formState.isSubmitting}>
              Lưu lại
            </Button>
          </div>
        </form>
      </Modal>

      {/* Delete Group Confirmation */}
      <ConfirmDialog
        isOpen={!!deletingGroupId}
        onClose={() => setDeletingGroupId(null)}
        onConfirm={handleConfirmDeleteGroup}
        title="Xóa nhóm tùy chọn"
        message="Bạn có chắc chắn muốn xóa nhóm này? Các tùy chọn bên trong cũng sẽ bị xóa."
        confirmText="Xóa bỏ"
        isConfirming={deleteGroupMutation.isPending}
      />

      {/* Delete Option Confirmation */}
      <ConfirmDialog
        isOpen={!!deletingOptionId}
        onClose={() => setDeletingOptionId(null)}
        onConfirm={handleConfirmDeleteOption}
        title="Xóa tùy chọn"
        message="Bạn có chắc chắn muốn xóa tùy chọn này khỏi nhóm?"
        confirmText="Xóa bỏ"
        isConfirming={deleteOptionMutation.isPending}
      />
    </div>
  );
}
