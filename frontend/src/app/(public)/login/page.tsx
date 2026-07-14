'use client';

import React from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import { loginSchema, type LoginInput } from '@/validations';
import { useAuthStore } from '@/stores/auth';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Card, CardHeader, CardContent } from '@/components/ui/card';
import { showToast } from '@/components/ui/toast';
import { api } from '@/lib/api';
import { Mail, Lock, Utensils } from 'lucide-react';

export default function LoginPage() {
  const router = useRouter();
  const { setAuth } = useAuthStore();
  
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<LoginInput>({
    resolver: zodResolver(loginSchema),
  });

  const onSubmit = async (data: LoginInput) => {
    try {
      const response = await api.post('/auth/login', data);
      const { accessToken, user } = response.data.data;

      setAuth(user, accessToken);
      showToast.success('Đăng nhập thành công!');

      // Redirect depending on user roles
      const role = user.roles[0];
      if (role === 'ADMIN') router.push('/admin');
      else if (role === 'MANAGER') router.push('/manager');
      else if (role === 'STAFF') router.push('/staff');
      else router.push('/customer');
      
    } catch (error: any) {
      const msg = error.response?.data?.message || 'Đăng nhập thất bại. Vui lòng kiểm tra lại thông tin.';
      showToast.error(msg);
    }
  };

  return (
    <div className="min-h-[calc(100vh-4rem-60px)] flex items-center justify-center p-4 bg-stone-50">
      <Card className="w-full max-w-md border-stone-200">
        <CardHeader className="text-center pb-2">
          <div className="mx-auto w-12 h-12 rounded-xl bg-primary-50 flex items-center justify-center text-primary-600 mb-3 border border-primary-100">
            <Utensils size={24} />
          </div>
          <h2 className="text-2xl font-bold text-stone-900 tracking-tight">Chào mừng trở lại</h2>
          <p className="text-sm text-stone-500 mt-1">Đăng nhập tài khoản hệ thống Phở Bò</p>
        </CardHeader>

        <CardContent>
          <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
            <Input
              label="Email"
              type="email"
              placeholder="example@gmail.com"
              icon={<Mail size={16} />}
              error={errors.email?.message}
              disabled={isSubmitting}
              {...register('email')}
            />

            <div>
              <div className="flex justify-between items-center mb-1">
                <label className="text-sm font-medium text-stone-700">Mật khẩu</label>
                <Link
                  href="/forgot-password"
                  className="text-xs font-semibold text-primary-600 hover:text-primary-700 transition-colors"
                >
                  Quên mật khẩu?
                </Link>
              </div>
              <Input
                type="password"
                placeholder="••••••••"
                icon={<Lock size={16} />}
                error={errors.password?.message}
                disabled={isSubmitting}
                {...register('password')}
              />
            </div>

            <Button
              type="submit"
              className="w-full mt-2"
              isLoading={isSubmitting}
            >
              Đăng nhập
            </Button>
          </form>

          <div className="mt-6 text-center text-sm text-stone-500">
            Chưa có tài khoản?{' '}
            <Link
              href="/register"
              className="font-semibold text-primary-600 hover:text-primary-700 transition-colors"
            >
              Đăng ký ngay
            </Link>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
