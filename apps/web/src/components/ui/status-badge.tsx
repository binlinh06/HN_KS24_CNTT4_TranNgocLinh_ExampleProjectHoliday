import React from 'react';

export interface StatusBadgeProps {
  status: string;
}

export const StatusBadge: React.FC<StatusBadgeProps> = ({ status }) => {
  const getBadgeStyle = (s: string) => {
    const defaultStyle = 'bg-stone-100 text-stone-800';
    const mapper: Record<string, string> = {
      // Order Status
      'CHO_XAC_NHAN': 'bg-amber-100 text-amber-800 border-amber-200',
      'DA_XAC_NHAN': 'bg-blue-100 text-blue-800 border-blue-200',
      'DANG_CHE_BIEN': 'bg-purple-100 text-purple-800 border-purple-200',
      'DANG_GIAO': 'bg-sky-100 text-sky-800 border-sky-200',
      'DANG_PHUC_VU': 'bg-pink-100 text-pink-800 border-pink-200',
      'HOAN_THANH': 'bg-emerald-100 text-emerald-800 border-emerald-200',
      'DA_HUY': 'bg-red-100 text-red-800 border-red-200',
      
      // Payment Status
      'PENDING': 'bg-amber-100 text-amber-800 border-amber-200',
      'SUCCESS': 'bg-emerald-100 text-emerald-800 border-emerald-200',
      'FAILED': 'bg-red-100 text-red-800 border-red-200',
      'REFUNDED': 'bg-stone-200 text-stone-800 border-stone-300',
      
      // Kitchen Item Status
      'CHO': 'bg-amber-100 text-amber-800',
      'DANG_NAU': 'bg-purple-100 text-purple-800',
      'DA_XONG': 'bg-emerald-100 text-emerald-800',
      
      // Table Status
      'AVAILABLE': 'bg-emerald-100 text-emerald-800 border-emerald-200',
      'OCCUPIED': 'bg-red-100 text-red-800 border-red-200',
      'CLEANING': 'bg-amber-100 text-amber-800 border-amber-200',
      'INACTIVE': 'bg-stone-100 text-stone-500 border-stone-200',
    };
    return mapper[s] || defaultStyle;
  };

  const formatText = (s: string) => {
    const textMapper: Record<string, string> = {
      'CHO_XAC_NHAN': 'Chờ xác nhận',
      'DA_XAC_NHAN': 'Đã xác nhận',
      'DANG_CHE_BIEN': 'Đang chế biến',
      'DANG_GIAO': 'Đang giao hàng',
      'DANG_PHUC_VU': 'Đang phục vụ',
      'HOAN_THANH': 'Hoàn thành',
      'DA_HUY': 'Đã hủy',
      'PENDING': 'Chờ thanh toán',
      'SUCCESS': 'Thành công',
      'FAILED': 'Thất bại',
      'REFUNDED': 'Đã hoàn tiền',
      'CHO': 'Chờ làm món',
      'DANG_NAU': 'Đang nấu',
      'DA_XONG': 'Đã xong',
      'AVAILABLE': 'Trống',
      'OCCUPIED': 'Có khách',
      'CLEANING': 'Đang dọn dẹp',
      'INACTIVE': 'Không hoạt động',
    };
    return textMapper[s] || s;
  };

  return (
    <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-semibold border ${getBadgeStyle(status)}`}>
      {formatText(status)}
    </span>
  );
};
