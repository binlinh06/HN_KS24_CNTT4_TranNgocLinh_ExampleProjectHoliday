import React from 'react';
import { AlertCircle } from 'lucide-react';
import { Button } from './button';

export interface ErrorStateProps {
  message?: string;
  onRetry?: () => void;
}

export const ErrorState: React.FC<ErrorStateProps> = ({
  message = 'Đã có lỗi xảy ra khi tải dữ liệu.',
  onRetry,
}) => {
  return (
    <div className="flex flex-col items-center justify-center p-12 text-center bg-red-50 border border-red-100 rounded-xl">
      <div className="p-4 bg-red-100 rounded-full text-red-600 mb-4">
        <AlertCircle size={32} />
      </div>
      <h3 className="text-base font-semibold text-red-900 mb-2">{message}</h3>
      {onRetry && (
        <Button variant="outline" size="sm" onClick={onRetry} className="bg-white border-red-200 text-red-700 hover:bg-red-50">
          Thử lại
        </Button>
      )}
    </div>
  );
};
