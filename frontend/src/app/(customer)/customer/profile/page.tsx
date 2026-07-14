'use client';

import React from 'react';
import { PageHeader } from '@/components/ui/page-header';

export default function CustomerCustomerProfilePage() {
  return (
    <div className="space-y-6">
      <PageHeader
        title="Thông tin cá nhân"
        description="Chi tiết đặc tả yêu cầu và trạng thái thiết kế màn hình."
      />
      
      <div className="p-6 bg-white rounded-xl border border-gray-200 shadow-sm">
        <div className="flex flex-col items-center justify-center py-12 text-center">
          <div className="w-16 h-16 bg-orange-50 text-orange-500 rounded-full flex items-center justify-center mb-4">
            <svg xmlns="http://www.w3.org/2000/svg" className="h-8 w-8" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 6V4m0 2a2 2 0 100 4m0-4a2 2 0 110 4m-6 8a2 2 0 100-4m0 4a2 2 0 110-4m0 4v2m0-6V4m6 6v10m6-2a2 2 0 100-4m0 4a2 2 0 110-4m0 4v2m0-6V4" />
            </svg>
          </div>
          <h3 className="text-lg font-semibold text-gray-900 mb-2">Đang phát triển</h3>
          <p className="text-gray-500 max-w-md">
            Giao diện cho chức năng này đang được thiết kế và phát triển đồng bộ với Backend APIs.
          </p>
        </div>
      </div>
    
    </div>
  );
}
