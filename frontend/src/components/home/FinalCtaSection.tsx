'use client';

import React from 'react';
import Link from 'next/link';
import { Button } from '@/components/ui/button';
import { ShoppingBag, ArrowRight, PhoneCall } from 'lucide-react';

export function FinalCtaSection() {
  return (
    <section className="py-20 bg-gradient-to-br from-[#084C38] via-[#0F6B4F] to-[#042F22] text-white relative overflow-hidden">
      {/* Background Radial Pattern */}
      <div className="absolute inset-0 bg-[radial-gradient(#C7A45B_1px,transparent_1px)] [background-size:24px_24px] opacity-15 pointer-events-none" />
      <div className="absolute -left-20 top-1/2 -translate-y-1/2 w-80 h-80 bg-[#C7A45B]/15 rounded-full blur-3xl pointer-events-none" />

      <div className="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8 text-center relative z-10 space-y-6">
        
        <div className="inline-flex items-center gap-2 px-3.5 py-1.5 rounded-full bg-[#C7A45B]/20 text-[#C7A45B] border border-[#C7A45B]/40 text-xs font-bold uppercase tracking-wider">
          <PhoneCall size={14} />
          ĐẶT MÓN GIAO TẬN NƠI
        </div>

        <h2 className="text-3xl sm:text-5xl font-extrabold tracking-tight leading-tight">
          Một Tô Phở Nóng Hổi <br />
          <span className="text-[#C7A45B]">Đang Chờ Bạn Thưởng Thức!</span>
        </h2>

        <p className="text-stone-200 text-sm sm:text-base max-w-2xl mx-auto leading-relaxed">
          Đăng ký tài khoản thành viên chỉ trong 1 phút để đặt món trực tuyến, áp dụng mã voucher giảm giá 
          và theo dõi trạng thái giao phở theo thời gian thực.
        </p>

        <div className="pt-4 flex flex-wrap justify-center gap-4">
          <Link href="/menu">
            <Button size="lg" className="h-13 px-8 bg-[#C7A45B] hover:bg-[#B89344] text-white font-extrabold text-base rounded-2xl shadow-xl gap-2.5 transition-all duration-300 hover:scale-[1.02]">
              <ShoppingBag size={20} />
              Đặt món ngay
            </Button>
          </Link>

          <Link href="/register">
            <Button size="lg" variant="outline" className="h-13 px-7 border-white/40 text-white hover:bg-white/10 font-bold text-base rounded-2xl gap-2 transition-all">
              Đăng ký thành viên
              <ArrowRight size={18} />
            </Button>
          </Link>
        </div>

      </div>
    </section>
  );
}
