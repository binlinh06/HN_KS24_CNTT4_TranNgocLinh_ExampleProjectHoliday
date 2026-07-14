'use client';

import React from 'react';
import { PageHeader } from '@/components/ui/page-header';

export default function ${safeName}Page() {
  return (
    <div className="space-y-6">
      <PageHeader
        title="Báo cáo Tài chính tổng thể"
        description="Chi tiết đặc tả yêu cầu và trạng thái thiết kế màn hình."
      />
      ${customBody}
    </div>
  );
}
