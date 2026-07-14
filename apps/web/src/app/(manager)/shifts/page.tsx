'use client';

import React from 'react';
import { PageHeader } from '@/components/ui/page-header';

export default function ${safeName}Page() {
  return (
    <div className="space-y-6">
      <PageHeader
        title="Lập lịch ca làm việc"
        description="Chi tiết đặc tả yêu cầu và trạng thái thiết kế màn hình."
      />
      ${customBody}
    </div>
  );
}
