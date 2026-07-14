'use client';

import React from 'react';
import { useRouter } from 'next/navigation';
import { useAuthStore, type AuthStatus } from '@/stores/auth';

interface AuthGuardProps {
  children: React.ReactNode;
  requiredRole: string;
}

export function AuthGuard({ children, requiredRole }: AuthGuardProps) {
  const router = useRouter();
  const { user, authStatus } = useAuthStore();

  // Still loading — show spinner
  if (authStatus === 'loading') {
    return (
      <div className="flex items-center justify-center min-h-screen bg-stone-50">
        <div className="flex flex-col items-center gap-3">
          <div className="w-10 h-10 animate-spin rounded-full border-[3px] border-stone-200 border-t-primary-500" />
          <span className="text-sm text-stone-500 font-medium">Đang xác thực...</span>
        </div>
      </div>
    );
  }

  // Not authenticated — redirect to login
  if (authStatus === 'unauthenticated' || !user) {
    if (typeof window !== 'undefined') {
      router.replace('/login');
    }
    return (
      <div className="flex items-center justify-center min-h-screen bg-stone-50">
        <div className="flex flex-col items-center gap-3">
          <div className="w-10 h-10 animate-spin rounded-full border-[3px] border-stone-200 border-t-primary-500" />
          <span className="text-sm text-stone-500 font-medium">Đang chuyển hướng...</span>
        </div>
      </div>
    );
  }

  // Authenticated but wrong role — redirect to unauthorized
  if (!user.roles.includes(requiredRole)) {
    if (typeof window !== 'undefined') {
      router.replace('/unauthorized');
    }
    return (
      <div className="flex items-center justify-center min-h-screen bg-stone-50">
        <div className="flex flex-col items-center gap-3">
          <div className="w-10 h-10 animate-spin rounded-full border-[3px] border-stone-200 border-t-primary-500" />
          <span className="text-sm text-stone-500 font-medium">Không có quyền truy cập...</span>
        </div>
      </div>
    );
  }

  // All checks passed
  return <>{children}</>;
}
