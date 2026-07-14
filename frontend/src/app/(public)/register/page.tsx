'use client';

import React from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import { registerSchema, type RegisterInput } from '@/validations';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Card, CardHeader, CardContent } from '@/components/ui/card';
import { showToast } from '@/components/ui/toast';
import { api } from '@/lib/api';
import { User, Mail, Phone, Lock, Utensils } from 'lucide-react';

export default function RegisterPage() {
  const router = useRouter();

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<RegisterInput>({
    resolver: zodResolver(registerSchema),
  });

  const onSubmit = async (data: RegisterInput) => {
    try {
      await api.post('/auth/register', data);
      showToast.success('Đăng ký tài khoản thành công! Vui lòng đăng nhập.');
      router.push('/login');
    } catch (error: any) {
      const msg = error.response?.data?.message || 'Đăng ký thất bại. Vui lòng kiểm tra lại thông tin.';
      showToast.error(msg);
    }
  };

  return (
    <div className="min-h-[calc(100vh-4rem-60px)] flex items-center justify-center p-4 bg-stone-50">
      <Card className="w-full max-w-lg border-stone-200">
        <CardHeader className="text-center pb-2">
          <div className="mx-auto w-12 h-12 rounded-xl bg-primary-50 flex items-center justify-center text-primary-600 mb-3 border border-primary-100">
            <Utensils size={24} />
          </div>
          <h2 className="text-2xl font-bold text-stone-900 tracking-tight">Tạo tài khoản mới</h2>
          <p className="text-sm text-stone-500 mt-1">Đăng ký để đặt món và nhận ngàn ưu đãi từ Phở Bò Gia Truyền</p>
        </CardHeader>

        <CardContent>
          <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
            <Input
              label="Họ tên"
              type="text"
              placeholder="Nguyễn Văn A"
              icon={<User size={16} />}
              error={errors.fullName?.message}
              disabled={isSubmitting}
              {...register('fullName')}
            />

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <Input
                label="Email"
                type="email"
                placeholder="example@gmail.com"
                icon={<Mail size={16} />}
                error={errors.email?.message}
                disabled={isSubmitting}
                {...register('email')}
              />

              <Input
                label="Số điện thoại"
                type="text"
                placeholder="0912345678"
                icon={<Phone size={16} />}
                error={errors.phone?.message}
                disabled={isSubmitting}
                {...register('phone')}
              />
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <Input
                label="Mật khẩu"
                type="password"
                placeholder="••••••••"
                icon={<Lock size={16} />}
                error={errors.password?.message}
                disabled={isSubmitting}
                {...register('password')}
              />

              <Input
                label="Xác nhận mật khẩu"
                type="password"
                placeholder="••••••••"
                icon={<Lock size={16} />}
                error={errors.confirmPassword?.message}
                disabled={isSubmitting}
                {...register('confirmPassword')}
              />
            </div>

            <div className="text-xs text-stone-500 bg-stone-50 p-3 rounded-lg border border-stone-200 space-y-1">
              <p className="font-semibold text-stone-700">Yêu cầu về mật khẩu:</p>
              <ul className="list-disc pl-4 space-y-0.5">
                <li>Tối thiểu 8 ký tự</li>
                <li>Chứa ít nhất 1 chữ cái viết hoa</li>
                <li>Chứa ít nhất 1 chữ số</li>
                <li>Chứa ít nhất 1 ký tự đặc biệt</li>
              </ul>
            </div>

            <Button
              type="submit"
              className="w-full mt-2"
              isLoading={isSubmitting}
            >
              Đăng ký tài khoản
            </Button>
          </form>

          <div className="mt-6 text-center text-sm text-stone-500">
            Đã có tài khoản?{' '}
            <Link
              href="/login"
              className="font-semibold text-primary-600 hover:text-primary-700 transition-colors"
            >
              Đăng nhập ngay
            </Link>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
