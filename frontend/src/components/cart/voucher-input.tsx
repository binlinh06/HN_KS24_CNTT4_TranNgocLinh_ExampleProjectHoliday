'use client';

import React, { useState } from 'react';
import { Tag, X, Loader2, CheckCircle } from 'lucide-react';
import { useApplyVoucher, useRemoveVoucher } from '@/features/cart/api';
import { Button } from '@/components/ui/button';
import { showToast } from '@/components/ui/toast';
import { VoucherSummary } from '@/types';

interface VoucherInputProps {
  appliedVoucher?: VoucherSummary;
  voucherRemovalReason?: string;
  discountAmount: number;
}

export function VoucherInput({
  appliedVoucher,
  voucherRemovalReason,
  discountAmount,
}: VoucherInputProps) {
  const [code, setCode] = useState('');
  const applyMutation = useApplyVoucher();
  const removeMutation = useRemoveVoucher();

  const handleApply = () => {
    const trimmed = code.trim().toUpperCase();
    if (!trimmed) {
      showToast.error('Vui lòng nhập mã giảm giá');
      return;
    }
    applyMutation.mutate(trimmed, {
      onSuccess: () => {
        showToast.success('Áp dụng mã giảm giá thành công!');
        setCode('');
      },
      onError: (error: any) => {
        const message =
          error.response?.data?.message || 'Mã giảm giá không hợp lệ';
        showToast.error(message);
      },
    });
  };

  const handleRemove = () => {
    removeMutation.mutate(undefined, {
      onSuccess: () => {
        showToast.success('Đã hủy mã giảm giá');
      },
      onError: () => {
        showToast.error('Không thể hủy mã giảm giá');
      },
    });
  };

  if (appliedVoucher) {
    return (
      <div className="space-y-2">
        <div className="flex items-center justify-between p-3 bg-green-50 border border-green-200 rounded-xl">
          <div className="flex items-center gap-2">
            <CheckCircle size={16} className="text-green-600" />
            <div>
              <span className="text-sm font-bold text-green-800">
                {appliedVoucher.code}
              </span>
              <p className="text-[11px] text-green-600">
                {appliedVoucher.discountType === 'PERCENTAGE'
                  ? `Giảm ${appliedVoucher.discountValue}%`
                  : `Giảm ${appliedVoucher.discountValue.toLocaleString('vi-VN')}đ`}
                {discountAmount > 0 && (
                  <span className="font-semibold">
                    {' '}
                    (−{discountAmount.toLocaleString('vi-VN')}đ)
                  </span>
                )}
              </p>
            </div>
          </div>
          <button
            onClick={handleRemove}
            disabled={removeMutation.isPending}
            className="p-1 text-green-600 hover:text-red-500 transition-colors"
          >
            {removeMutation.isPending ? (
              <Loader2 size={14} className="animate-spin" />
            ) : (
              <X size={14} />
            )}
          </button>
        </div>
        {voucherRemovalReason && (
          <p className="text-[11px] text-amber-600 px-1">
            ⚠️ {voucherRemovalReason}
          </p>
        )}
      </div>
    );
  }

  return (
    <div className="space-y-2">
      <label className="text-sm font-semibold text-stone-700 flex items-center gap-1.5">
        <Tag size={14} />
        Mã giảm giá
      </label>
      <div className="flex gap-2">
        <input
          type="text"
          value={code}
          onChange={(e) => setCode(e.target.value.toUpperCase())}
          onKeyDown={(e) => e.key === 'Enter' && handleApply()}
          placeholder="Nhập mã giảm giá..."
          className="flex-1 text-sm border border-stone-200 rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent placeholder:text-stone-400 uppercase"
          disabled={applyMutation.isPending}
        />
        <Button
          onClick={handleApply}
          disabled={applyMutation.isPending || !code.trim()}
          variant="outline"
          className="shrink-0"
        >
          {applyMutation.isPending ? (
            <Loader2 size={14} className="animate-spin" />
          ) : (
            'Áp dụng'
          )}
        </Button>
      </div>
    </div>
  );
}
