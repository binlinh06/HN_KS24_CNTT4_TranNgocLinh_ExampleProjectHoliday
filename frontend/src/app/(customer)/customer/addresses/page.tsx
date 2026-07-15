'use client';

import React, { useState } from 'react';
import { PageHeader } from '@/components/ui/page-header';
import {
  useAddresses,
  useCreateAddress,
  useUpdateAddress,
  useDeleteAddress,
  useSetDefaultAddress,
} from '@/features/addresses/api';
import { AddressResponse } from '@/types';

export default function CustomerAddressesPage() {
  const { data: addresses, isLoading, isError } = useAddresses();
  const createAddressMutation = useCreateAddress();
  const updateAddressMutation = useUpdateAddress();
  const deleteAddressMutation = useDeleteAddress();
  const setDefaultMutation = useSetDefaultAddress();

  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingAddress, setEditingAddress] = useState<AddressResponse | null>(null);

  // Form State
  const [receiverName, setReceiverName] = useState('');
  const [receiverPhone, setReceiverPhone] = useState('');
  const [addressDetail, setAddressDetail] = useState('');
  const [addressLabel, setAddressLabel] = useState('Nhà riêng');
  const [isDefault, setIsDefault] = useState(false);
  const [validationError, setValidationError] = useState('');

  const openAddModal = () => {
    setEditingAddress(null);
    setReceiverName('');
    setReceiverPhone('');
    setAddressDetail('');
    setAddressLabel('Nhà riêng');
    setIsDefault(false);
    setValidationError('');
    setIsModalOpen(true);
  };

  const openEditModal = (addr: AddressResponse) => {
    setEditingAddress(addr);
    setReceiverName(addr.receiverName);
    setReceiverPhone(addr.receiverPhone);
    setAddressDetail(addr.addressDetail);
    setAddressLabel(addr.addressLabel || 'Nhà riêng');
    setIsDefault(addr.isDefault);
    setValidationError('');
    setIsModalOpen(true);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!receiverName.trim() || !receiverPhone.trim() || !addressDetail.trim()) {
      setValidationError('Vui lòng điền đầy đủ các thông tin bắt buộc.');
      return;
    }

    const phoneRegex = /^[0-9]{9,11}$/;
    if (!phoneRegex.test(receiverPhone.trim())) {
      setValidationError('Số điện thoại không hợp lệ. Phải chứa từ 9 đến 11 số.');
      return;
    }

    const payload = {
      receiverName: receiverName.trim(),
      receiverPhone: receiverPhone.trim(),
      addressDetail: addressDetail.trim(),
      addressLabel: addressLabel.trim(),
      isDefault,
    };

    try {
      if (editingAddress) {
        await updateAddressMutation.mutateAsync({ id: editingAddress.id, data: payload });
      } else {
        await createAddressMutation.mutateAsync(payload);
      }
      setIsModalOpen(false);
    } catch (err: any) {
      setValidationError(err.response?.data?.message || 'Có lỗi xảy ra, vui lòng thử lại.');
    }
  };

  const handleDelete = async (id: string) => {
    if (confirm('Bạn có chắc chắn muốn xóa địa chỉ này?')) {
      try {
        await deleteAddressMutation.mutateAsync(id);
      } catch (err: any) {
        alert(err.response?.data?.message || 'Không thể xóa địa chỉ này.');
      }
    }
  };

  const handleSetDefault = async (id: string) => {
    try {
      await setDefaultMutation.mutateAsync(id);
    } catch (err: any) {
      alert(err.response?.data?.message || 'Có lỗi xảy ra.');
    }
  };

  return (
    <div className="space-y-6 max-w-4xl mx-auto px-4 py-6">
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <PageHeader
          title="Sổ địa chỉ giao hàng"
          description="Quản lý thông tin địa chỉ giao hàng phở của bạn."
        />
        <button
          onClick={openAddModal}
          className="inline-flex items-center justify-center px-4 py-2 bg-[#0F6B4F] hover:bg-[#0c5942] text-white font-medium rounded-lg transition-colors shadow-sm focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-[#0F6B4F]"
        >
          <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 mr-1.5" viewBox="0 0 20 20" fill="currentColor">
            <path fillRule="evenodd" d="M10 5a1 1 0 011 1v3h3a1 1 0 110 2h-3v3a1 1 0 11-2 0v-3H6a1 1 0 110-2h3V6a1 1 0 011-1z" clipRule="evenodd" />
          </svg>
          Thêm địa chỉ mới
        </button>
      </div>

      {isLoading && (
        <div className="flex justify-center items-center py-16">
          <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-[#0F6B4F]"></div>
        </div>
      )}

      {isError && (
        <div className="bg-red-50 border border-red-200 text-red-700 p-4 rounded-lg">
          Lấy danh sách địa chỉ thất bại. Vui lòng thử lại.
        </div>
      )}

      {addresses && addresses.length === 0 && (
        <div className="p-8 bg-white border border-gray-150 rounded-xl shadow-sm text-center">
          <div className="w-16 h-16 bg-gray-100 text-gray-400 rounded-full flex items-center justify-center mx-auto mb-4">
            <svg xmlns="http://www.w3.org/2000/svg" className="h-8 w-8" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z" />
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 11a3 3 0 11-6 0 3 3 0 016 0z" />
            </svg>
          </div>
          <h3 className="text-lg font-semibold text-gray-800 mb-2">Chưa có địa chỉ nào</h3>
          <p className="text-gray-500 mb-4">Vui lòng thêm địa chỉ của bạn để đặt hàng thuận tiện nhất.</p>
          <button
            onClick={openAddModal}
            className="px-4 py-2 border border-[#0F6B4F] text-[#0F6B4F] hover:bg-green-50 font-medium rounded-lg transition-colors"
          >
            Thêm địa chỉ đầu tiên
          </button>
        </div>
      )}

      {addresses && addresses.length > 0 && (
        <div className="grid gap-4">
          {addresses.map((addr) => (
            <div
              key={addr.id}
              className={`p-5 bg-white border rounded-xl shadow-sm transition-all hover:shadow-md flex flex-col md:flex-row md:items-start justify-between gap-4 ${
                addr.isDefault ? 'border-[#0F6B4F] ring-1 ring-[#0F6B4F] ring-opacity-20' : 'border-gray-200'
              }`}
            >
              <div className="space-y-2">
                <div className="flex flex-wrap items-center gap-2">
                  <span className="font-semibold text-gray-900">{addr.receiverName}</span>
                  <span className="text-gray-400">|</span>
                  <span className="text-gray-600 font-medium">{addr.receiverPhone}</span>
                  {addr.addressLabel && (
                    <span className="px-2 py-0.5 text-xs font-semibold bg-gray-100 text-gray-600 rounded">
                      {addr.addressLabel}
                    </span>
                  )}
                  {addr.isDefault && (
                    <span className="px-2.5 py-0.5 text-xs font-semibold bg-emerald-50 text-[#0F6B4F] border border-emerald-200 rounded">
                      Mặc định
                    </span>
                  )}
                </div>
                <p className="text-gray-600 text-sm">{addr.addressDetail}</p>
              </div>

              <div className="flex flex-wrap items-center gap-3 self-end md:self-center">
                {!addr.isDefault && (
                  <button
                    onClick={() => handleSetDefault(addr.id)}
                    className="text-xs text-gray-600 hover:text-gray-900 border border-gray-300 rounded px-2.5 py-1 hover:bg-gray-50 transition-colors"
                  >
                    Đặt làm mặc định
                  </button>
                )}
                <button
                  onClick={() => openEditModal(addr)}
                  className="text-xs text-[#C7A45B] hover:text-[#b08f4c] font-semibold flex items-center"
                >
                  <svg xmlns="http://www.w3.org/2000/svg" className="h-3.5 w-3.5 mr-1" viewBox="0 0 20 20" fill="currentColor">
                    <path d="M13.586 3.586a2 2 0 112.828 2.828l-.793.793-2.828-2.828.793-.793zM11.379 5.793L3 14.172V17h2.828l8.38-8.379-2.83-2.828z" />
                  </svg>
                  Sửa
                </button>
                <button
                  onClick={() => handleDelete(addr.id)}
                  className="text-xs text-red-500 hover:text-red-700 font-semibold flex items-center"
                >
                  <svg xmlns="http://www.w3.org/2000/svg" className="h-3.5 w-3.5 mr-1" viewBox="0 0 20 20" fill="currentColor">
                    <path fillRule="evenodd" d="M9 2a1 1 0 00-.894.553L7.382 4H4a1 1 0 000 2v10a2 2 0 002 2h8a2 2 0 002-2V6a1 1 0 100-2h-3.382l-.724-1.447A1 1 0 0011 2H9zM7 8a1 1 0 012 0v6a1 1 0 11-2 0V8zm5-1a1 1 0 00-1 1v6a1 1 0 102 0V8a1 1 0 00-1-1z" clipRule="evenodd" />
                  </svg>
                  Xóa
                </button>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Modal Add/Edit */}
      {isModalOpen && (
        <div className="fixed inset-0 bg-black bg-opacity-50 z-50 flex items-center justify-center p-4">
          <div className="bg-white rounded-xl max-w-md w-full p-6 shadow-2xl animate-fade-in relative">
            <h3 className="text-lg font-bold text-gray-900 mb-4">
              {editingAddress ? 'Sửa địa chỉ giao hàng' : 'Thêm địa chỉ mới'}
            </h3>

            <form onSubmit={handleSubmit} className="space-y-4">
              {validationError && (
                <div className="text-sm bg-red-50 border border-red-200 text-red-600 p-3 rounded">
                  {validationError}
                </div>
              )}

              <div>
                <label className="block text-xs font-semibold text-gray-700 uppercase tracking-wider mb-1">
                  Tên người nhận <span className="text-red-500">*</span>
                </label>
                <input
                  type="text"
                  value={receiverName}
                  onChange={(e) => setReceiverName(e.target.value)}
                  placeholder="Ví dụ: Nguyễn Văn A"
                  className="w-full border border-gray-300 rounded-lg px-3.5 py-2 text-sm focus:outline-none focus:ring-1 focus:ring-[#0F6B4F] focus:border-[#0F6B4F]"
                  required
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-gray-700 uppercase tracking-wider mb-1">
                  Số điện thoại người nhận <span className="text-red-500">*</span>
                </label>
                <input
                  type="tel"
                  value={receiverPhone}
                  onChange={(e) => setReceiverPhone(e.target.value)}
                  placeholder="Ví dụ: 0987654321"
                  className="w-full border border-gray-300 rounded-lg px-3.5 py-2 text-sm focus:outline-none focus:ring-1 focus:ring-[#0F6B4F] focus:border-[#0F6B4F]"
                  required
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-gray-700 uppercase tracking-wider mb-1">
                  Địa chỉ chi tiết <span className="text-red-500">*</span>
                </label>
                <textarea
                  value={addressDetail}
                  onChange={(e) => setAddressDetail(e.target.value)}
                  placeholder="Số nhà, ngõ/ngách, tên đường, phường/xã, quận/huyện..."
                  rows={3}
                  className="w-full border border-gray-300 rounded-lg px-3.5 py-2 text-sm focus:outline-none focus:ring-1 focus:ring-[#0F6B4F] focus:border-[#0F6B4F]"
                  required
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-gray-700 uppercase tracking-wider mb-1">
                  Nhãn địa chỉ
                </label>
                <div className="flex gap-2">
                  {['Nhà riêng', 'Văn phòng', 'Khác'].map((lbl) => (
                    <button
                      key={lbl}
                      type="button"
                      onClick={() => setAddressLabel(lbl)}
                      className={`flex-1 text-center py-1.5 text-xs font-medium border rounded-md transition-colors ${
                        addressLabel === lbl
                          ? 'border-[#0F6B4F] bg-green-50 text-[#0F6B4F]'
                          : 'border-gray-300 text-gray-600 hover:bg-gray-50'
                      }`}
                    >
                      {lbl}
                    </button>
                  ))}
                </div>
              </div>

              <div className="flex items-center gap-2 pt-1">
                <input
                  type="checkbox"
                  id="chkDefault"
                  checked={isDefault}
                  disabled={editingAddress?.isDefault} // Can't un-default the default one directly, must set another default
                  onChange={(e) => setIsDefault(e.target.checked)}
                  className="h-4 w-4 text-[#0F6B4F] focus:ring-[#0F6B4F] border-gray-300 rounded"
                />
                <label htmlFor="chkDefault" className="text-sm text-gray-700 font-medium cursor-pointer">
                  Đặt làm địa chỉ giao hàng mặc định
                </label>
              </div>

              <div className="flex gap-3 pt-3 border-t border-gray-150">
                <button
                  type="button"
                  onClick={() => setIsModalOpen(false)}
                  className="flex-1 py-2 border border-gray-300 hover:bg-gray-50 text-gray-700 font-medium rounded-lg text-sm transition-colors"
                >
                  Hủy
                </button>
                <button
                  type="submit"
                  disabled={createAddressMutation.isPending || updateAddressMutation.isPending}
                  className="flex-1 py-2 bg-[#0F6B4F] hover:bg-[#0c5942] disabled:opacity-50 text-white font-medium rounded-lg text-sm transition-colors flex items-center justify-center"
                >
                  {(createAddressMutation.isPending || updateAddressMutation.isPending) ? (
                    <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-white"></div>
                  ) : (
                    'Lưu địa chỉ'
                  )}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
