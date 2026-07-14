'use client';

import React from 'react';
import Link from 'next/link';
import { Button } from '@/components/ui/button';
import { Card, CardContent } from '@/components/ui/card';
import { ShieldAlert, ArrowLeft, Home } from 'lucide-react';

export default function UnauthorizedPage() {
  return (
    <div className="min-h-[calc(100vh-4rem-60px)] flex items-center justify-center p-4 bg-stone-50">
      <Card className="w-full max-w-md border-stone-200 text-center">
        <CardContent className="pt-8 pb-8">
          <div className="mx-auto w-14 h-14 rounded-full bg-error-50 flex items-center justify-center text-error-600 mb-6 border border-error-100">
            <ShieldAlert size={28} />
          </div>
          
          <h1 className="text-3xl font-extrabold text-stone-900 tracking-tight">403</h1>
          <h2 className="text-lg font-bold text-stone-850 mt-2">Truy cập bị từ chối</h2>
          
          <p className="text-stone-500 text-sm mt-3 leading-relaxed">
            Bạn không có quyền truy cập vào đường dẫn này. Vui lòng kiểm tra lại tài khoản hoặc liên hệ quản trị viên để biết thêm chi tiết.
          </p>

          <div className="mt-8 flex flex-col sm:flex-row gap-3 justify-center">
            <Link href="/login" className="w-full sm:w-auto">
              <Button variant="outline" className="w-full gap-2">
                <ArrowLeft size={16} />
                Đăng nhập lại
              </Button>
            </Link>
            <Link href="/" className="w-full sm:w-auto">
              <Button className="w-full gap-2 bg-primary-500 hover:bg-primary-600">
                <Home size={16} />
                Về trang chủ
              </Button>
            </Link>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
