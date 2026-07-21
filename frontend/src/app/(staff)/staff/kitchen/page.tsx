'use client';

import React, { useState, useEffect } from 'react';
import { PageHeader } from '@/components/ui/page-header';
import { useKitchenQueue, updateKitchenItemStatus } from '@/features/staff/api';
import { useStaffSSE } from '@/features/staff/hooks/useStaffSSE';
import { useQueryClient } from '@tanstack/react-query';
import { staffKeys } from '@/features/staff/api';
import { toast } from 'react-hot-toast';

export default function KitchenQueuePage() {
  // Realtime updates
  useStaffSSE('kitchen');

  const queryClient = useQueryClient();
  const { data: queueItems = [], isLoading, error } = useKitchenQueue();
  const [actionLoading, setActionLoading] = useState<string | null>(null);
  const [now, setNow] = useState<Date>(new Date());

  // Tick for wait times
  useEffect(() => {
    const timer = setInterval(() => setNow(new Date()), 10000); // refresh elapsed time every 10s
    return () => clearInterval(timer);
  }, []);

  const handleUpdateStatus = async (queueId: string, status: string, version: number) => {
    setActionLoading(queueId);
    try {
      await updateKitchenItemStatus(queueId, status, version);
      toast.success('Cập nhật trạng thái nhà bếp thành công');
      queryClient.invalidateQueries({ queryKey: staffKeys.kitchenQueue });
    } catch (err: any) {
      toast.error(err.response?.data?.message || 'Có lỗi xảy ra khi cập nhật trạng thái');
    } finally {
      setActionLoading(null);
    }
  };

  const getElapsedTime = (createdTimeStr: string) => {
    const created = new Date(createdTimeStr);
    const diffMs = now.getTime() - created.getTime();
    const diffMins = Math.floor(diffMs / 60000);
    if (diffMins < 1) return 'Vừa xong';
    return `${diffMins} phút trước`;
  };

  const getElapsedTimeColor = (createdTimeStr: string) => {
    const created = new Date(createdTimeStr);
    const diffMs = now.getTime() - created.getTime();
    const diffMins = Math.floor(diffMs / 60000);
    if (diffMins >= 15) return 'text-red-600 font-bold animate-pulse';
    if (diffMins >= 8) return 'text-amber-600 font-bold';
    return 'text-gray-500';
  };

  // Separate items by status
  const waitingItems = queueItems.filter(item => item.status === 'CHO');
  const cookingItems = queueItems.filter(item => item.status === 'DANG_NAU');
  const readyItems = queueItems.filter(item => item.status === 'DA_XONG');

  return (
    <div className="space-y-6">
      <PageHeader
        title="Màn hình nhà bếp (KDS)"
        description="Theo dõi hàng đợi chế biến món ăn, cập nhật trạng thái nấu nướng theo thời gian thực."
      />

      {isLoading ? (
        <div className="flex justify-center py-12">
          <div className="animate-spin rounded-full h-10 w-10 border-t-2 border-b-2 border-emerald-600"></div>
        </div>
      ) : error ? (
        <div className="p-4 bg-red-50 text-red-700 border border-red-200 rounded-xl text-center">
          Lỗi kết nối hàng đợi nhà bếp. Vui lòng tải lại trang.
        </div>
      ) : (
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          {/* Waiting column */}
          <div className="bg-gray-50 rounded-xl border border-gray-200 p-4 space-y-4">
            <div className="flex items-center justify-between border-b border-gray-200 pb-2">
              <h3 className="font-bold text-gray-800 flex items-center gap-2">
                <span className="w-2.5 h-2.5 bg-amber-500 rounded-full"></span>
                Chờ chế biến ({waitingItems.length})
              </h3>
            </div>
            <div className="space-y-3 overflow-y-auto max-h-[70vh] pr-1">
              {waitingItems.map((item) => (
                <div 
                  key={item.id}
                  className="p-4 bg-white rounded-lg border border-gray-200 shadow-sm space-y-3 hover:border-amber-400 transition-colors"
                >
                  <div className="flex items-start justify-between">
                    <div>
                      <span className="text-xs text-emerald-700 font-bold bg-emerald-50 px-2 py-0.5 rounded">
                        {item.tableNumber ? `Bàn ${item.tableNumber}` : 'Mang về'}
                      </span>
                      <div className="text-[10px] text-gray-400 mt-1">Đơn: {item.orderCode}</div>
                    </div>
                    <span className={`text-xs ${getElapsedTimeColor(item.createdAt)}`}>
                      {getElapsedTime(item.createdAt)}
                    </span>
                  </div>

                  <div className="border-t border-gray-100 pt-2">
                    <div className="font-bold text-gray-900 text-base">
                      {item.productName} <span className="text-emerald-700">x{item.quantity}</span>
                    </div>
                    {item.options && item.options.length > 0 && (
                      <div className="text-xs text-gray-500 font-semibold mt-1">
                        Tùy chọn: {item.options.join(', ')}
                      </div>
                    )}
                  </div>

                  <button
                    onClick={() => handleUpdateStatus(item.id, 'DANG_NAU', item.version)}
                    disabled={actionLoading === item.id}
                    className="w-full py-1.5 bg-amber-500 hover:bg-amber-600 text-white rounded font-semibold text-xs shadow-sm disabled:opacity-50"
                  >
                    Bắt đầu nấu
                  </button>
                </div>
              ))}
              {waitingItems.length === 0 && (
                <p className="text-sm text-gray-400 text-center py-8">Không có món chờ.</p>
              )}
            </div>
          </div>

          {/* Cooking column */}
          <div className="bg-gray-50 rounded-xl border border-gray-200 p-4 space-y-4">
            <div className="flex items-center justify-between border-b border-gray-200 pb-2">
              <h3 className="font-bold text-gray-800 flex items-center gap-2">
                <span className="w-2.5 h-2.5 bg-blue-500 rounded-full"></span>
                Đang chế biến ({cookingItems.length})
              </h3>
            </div>
            <div className="space-y-3 overflow-y-auto max-h-[70vh] pr-1">
              {cookingItems.map((item) => (
                <div 
                  key={item.id}
                  className="p-4 bg-white rounded-lg border border-gray-200 shadow-sm space-y-3 hover:border-blue-400 transition-colors"
                >
                  <div className="flex items-start justify-between">
                    <div>
                      <span className="text-xs text-emerald-700 font-bold bg-emerald-50 px-2 py-0.5 rounded">
                        {item.tableNumber ? `Bàn ${item.tableNumber}` : 'Mang về'}
                      </span>
                      <div className="text-[10px] text-gray-400 mt-1">Đơn: {item.orderCode}</div>
                    </div>
                    <span className={`text-xs ${getElapsedTimeColor(item.createdAt)}`}>
                      {getElapsedTime(item.createdAt)}
                    </span>
                  </div>

                  <div className="border-t border-gray-100 pt-2">
                    <div className="font-bold text-gray-900 text-base">
                      {item.productName} <span className="text-emerald-700">x{item.quantity}</span>
                    </div>
                    {item.options && item.options.length > 0 && (
                      <div className="text-xs text-gray-500 font-semibold mt-1">
                        Tùy chọn: {item.options.join(', ')}
                      </div>
                    )}
                  </div>

                  <button
                    onClick={() => handleUpdateStatus(item.id, 'DA_XONG', item.version)}
                    disabled={actionLoading === item.id}
                    className="w-full py-1.5 bg-blue-500 hover:bg-blue-600 text-white rounded font-semibold text-xs shadow-sm disabled:opacity-50"
                  >
                    Hoàn thành chế biến
                  </button>
                </div>
              ))}
              {cookingItems.length === 0 && (
                <p className="text-sm text-gray-400 text-center py-8">Không có món đang nấu.</p>
              )}
            </div>
          </div>

          {/* Ready column */}
          <div className="bg-gray-50 rounded-xl border border-gray-200 p-4 space-y-4">
            <div className="flex items-center justify-between border-b border-gray-200 pb-2">
              <h3 className="font-bold text-gray-800 flex items-center gap-2">
                <span className="w-2.5 h-2.5 bg-emerald-500 rounded-full"></span>
                Chờ phục vụ/bàn giao ({readyItems.length})
              </h3>
            </div>
            <div className="space-y-3 overflow-y-auto max-h-[70vh] pr-1">
              {readyItems.map((item) => (
                <div 
                  key={item.id}
                  className="p-4 bg-white rounded-lg border border-gray-200 shadow-sm space-y-2 border-emerald-200"
                >
                  <div className="flex items-start justify-between">
                    <div>
                      <span className="text-xs text-emerald-700 font-bold bg-emerald-50 px-2 py-0.5 rounded">
                        {item.tableNumber ? `Bàn ${item.tableNumber}` : 'Mang về'}
                      </span>
                      <div className="text-[10px] text-gray-400 mt-1">Đơn: {item.orderCode}</div>
                    </div>
                  </div>

                  <div className="border-t border-gray-100 pt-2">
                    <div className="font-bold text-gray-700 text-base line-through">
                      {item.productName} <span className="text-gray-400">x{item.quantity}</span>
                    </div>
                  </div>
                </div>
              ))}
              {readyItems.length === 0 && (
                <p className="text-sm text-gray-400 text-center py-8">Không có món đã xong.</p>
              )}
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
