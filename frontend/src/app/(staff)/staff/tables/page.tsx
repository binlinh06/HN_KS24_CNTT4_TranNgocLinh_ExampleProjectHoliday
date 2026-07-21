'use client';

import React, { useState } from 'react';
import { PageHeader } from '@/components/ui/page-header';
import { 
  useStaffTables, 
  openStaffTable, 
  markTableCleaning, 
  markTableAvailable, 
  markTableOutOfService, 
  restoreTable 
} from '@/features/staff/api';
import { useStaffSSE } from '@/features/staff/hooks/useStaffSSE';
import { useQueryClient } from '@tanstack/react-query';
import { staffKeys } from '@/features/staff/api';
import { toast } from 'react-hot-toast';

export default function StaffTablesPage() {
  // Realtime updates
  useStaffSSE('orders');
  
  const queryClient = useQueryClient();
  const { data: tables = [], isLoading, error } = useStaffTables();
  const [actionLoading, setActionLoading] = useState<string | null>(null);

  const handleAction = async (tableId: string, actionName: string, actionFn: (id: string) => Promise<any>) => {
    setActionLoading(`${tableId}-${actionName}`);
    try {
      await actionFn(tableId);
      toast.success('Cập nhật trạng thái bàn ăn thành công');
      queryClient.invalidateQueries({ queryKey: staffKeys.tables });
    } catch (err: any) {
      toast.error(err.response?.data?.message || 'Có lỗi xảy ra khi cập nhật bàn ăn');
    } finally {
      setActionLoading(null);
    }
  };

  const getStatusBadge = (status: string) => {
    switch (status) {
      case 'AVAILABLE':
        return (
          <span className="px-2.5 py-1 text-xs font-semibold rounded-full bg-emerald-50 text-emerald-700 border border-emerald-200">
            Trống (Sẵn sàng)
          </span>
        );
      case 'OCCUPIED':
        return (
          <span className="px-2.5 py-1 text-xs font-semibold rounded-full bg-amber-50 text-amber-700 border border-amber-200">
            Có khách
          </span>
        );
      case 'CLEANING':
        return (
          <span className="px-2.5 py-1 text-xs font-semibold rounded-full bg-blue-50 text-blue-700 border border-blue-200 animate-pulse">
            Đang dọn dẹp
          </span>
        );
      case 'INACTIVE':
        return (
          <span className="px-2.5 py-1 text-xs font-semibold rounded-full bg-gray-100 text-gray-500 border border-gray-300">
            Tạm ngưng phục vụ
          </span>
        );
      default:
        return (
          <span className="px-2.5 py-1 text-xs font-semibold rounded-full bg-gray-50 text-gray-700">
            {status}
          </span>
        );
    }
  };

  return (
    <div className="space-y-6">
      <PageHeader
        title="Sơ đồ bàn ăn"
        description="Quản lý sơ đồ bàn, mở bàn mới và cập nhật trạng thái dọn dẹp bàn ăn theo thời gian thực."
      />

      {isLoading ? (
        <div className="flex justify-center py-12">
          <div className="animate-spin rounded-full h-10 w-10 border-t-2 border-b-2 border-emerald-600"></div>
        </div>
      ) : error ? (
        <div className="p-4 bg-red-50 text-red-700 border border-red-200 rounded-xl text-center">
          Lỗi tải dữ liệu sơ đồ bàn ăn. Vui lòng tải lại trang.
        </div>
      ) : tables.length === 0 ? (
        <div className="p-6 bg-white rounded-xl border border-gray-200 shadow-sm text-center py-12">
          <p className="text-gray-500">Chưa cấu hình bàn ăn nào trong hệ thống.</p>
        </div>
      ) : (
        <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-6">
          {tables.map((table) => {
            const isBusy = actionLoading?.startsWith(table.id);

            return (
              <div 
                key={table.id}
                className="bg-white rounded-xl border border-gray-200 shadow-sm overflow-hidden flex flex-col justify-between hover:shadow-md transition-shadow duration-200"
              >
                <div className="p-5 space-y-4">
                  <div className="flex items-center justify-between">
                    <h3 className="text-lg font-bold text-gray-900">Bàn số {table.tableNumber}</h3>
                    {getStatusBadge(table.status)}
                  </div>
                  
                  <div className="text-sm text-gray-500">
                    Sức chứa: <span className="font-semibold text-gray-800">{table.capacity} chỗ</span>
                  </div>
                </div>

                <div className="p-4 bg-gray-50 border-t border-gray-100 flex flex-wrap gap-2 justify-end">
                  {table.status === 'AVAILABLE' && (
                    <>
                      <button
                        onClick={() => handleAction(table.id, 'open', openStaffTable)}
                        disabled={isBusy}
                        className="px-3 py-1.5 text-xs font-semibold text-white bg-emerald-600 hover:bg-emerald-700 rounded-lg shadow-sm disabled:opacity-50 transition-colors"
                      >
                        Mở bàn
                      </button>
                      <button
                        onClick={() => handleAction(table.id, 'inactive', markTableOutOfService)}
                        disabled={isBusy}
                        className="px-3 py-1.5 text-xs font-semibold text-gray-700 bg-white border border-gray-300 hover:bg-gray-50 rounded-lg disabled:opacity-50 transition-colors"
                      >
                        Tạm dừng
                      </button>
                    </>
                  )}

                  {table.status === 'OCCUPIED' && (
                    <button
                      onClick={() => handleAction(table.id, 'cleaning', markTableCleaning)}
                      disabled={isBusy}
                      className="px-3 py-1.5 text-xs font-semibold text-white bg-blue-600 hover:bg-blue-700 rounded-lg shadow-sm disabled:opacity-50 transition-colors"
                    >
                      Dọn dẹp
                    </button>
                  )}

                  {table.status === 'CLEANING' && (
                    <>
                      <button
                        onClick={() => handleAction(table.id, 'available', markTableAvailable)}
                        disabled={isBusy}
                        className="px-3 py-1.5 text-xs font-semibold text-white bg-emerald-600 hover:bg-emerald-700 rounded-lg shadow-sm disabled:opacity-50 transition-colors"
                      >
                        Dọn xong
                      </button>
                      <button
                        onClick={() => handleAction(table.id, 'inactive', markTableOutOfService)}
                        disabled={isBusy}
                        className="px-3 py-1.5 text-xs font-semibold text-gray-700 bg-white border border-gray-300 hover:bg-gray-50 rounded-lg disabled:opacity-50 transition-colors"
                      >
                        Tạm dừng
                      </button>
                    </>
                  )}

                  {table.status === 'INACTIVE' && (
                    <button
                      onClick={() => handleAction(table.id, 'restore', restoreTable)}
                      disabled={isBusy}
                      className="px-3 py-1.5 text-xs font-semibold text-white bg-emerald-600 hover:bg-emerald-700 rounded-lg shadow-sm disabled:opacity-50 transition-colors"
                    >
                      Khôi phục
                    </button>
                  )}
                </div>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
}
