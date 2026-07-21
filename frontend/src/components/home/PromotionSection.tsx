'use client';

import React from 'react';
import Link from 'next/link';
import { Card, CardContent } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { mockVouchers } from '@/mocks/dashboard';
import { Ticket, ArrowRight, Sparkles } from 'lucide-react';

export function PromotionSection() {
  return (
    <section id="promotions" className="py-20 bg-[#084C38] text-white relative overflow-hidden">
      {/* Background Decorative Graphic */}
      <div className="absolute inset-0 bg-[radial-gradient(#C7A45B_1px,transparent_1px)] [background-size:24px_24px] opacity-15 pointer-events-none" />
      <div className="absolute -right-24 -bottom-24 w-96 h-96 bg-[#C7A45B]/15 rounded-full blur-3xl pointer-events-none" />

      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 relative z-10">
        
        {/* Header */}
        <div className="text-center max-w-2xl mx-auto mb-14 space-y-3">
          <div className="inline-flex items-center gap-2 px-3.5 py-1.5 rounded-full bg-[#C7A45B]/20 text-[#C7A45B] border border-[#C7A45B]/40 text-xs font-bold uppercase tracking-wider">
            <Sparkles size={14} />
            ƯU ĐÃI ĐẶC BIỆT
          </div>
          <h2 className="text-3xl sm:text-4xl font-extrabold text-white tracking-tight">
            Chương Trình <span className="text-[#C7A45B]">Khuyến Mãi Hấp Dẫn</span>
          </h2>
          <p className="text-stone-300 text-sm sm:text-base leading-relaxed">
            Nhận ngay các mã giảm giá hấp dẫn khi đăng ký tài khoản thành viên và đặt món trực tuyến.
          </p>
        </div>

        {/* Vouchers Grid */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
          {mockVouchers.map((v, index) => (
            <Card 
              key={index} 
              className="bg-white/95 backdrop-blur-md border border-[#C7A45B]/40 hover:border-[#C7A45B] text-[#1F2937] rounded-2xl shadow-xl hover:scale-[1.02] transition-all duration-300 overflow-hidden"
            >
              <CardContent className="p-7 flex flex-col justify-between h-full space-y-6">
                <div className="space-y-3">
                  <div className="flex items-center justify-between">
                    <Badge className="bg-[#0F6B4F] text-white font-bold text-xs px-2.5 py-1">
                      GIẢM {v.discount}
                    </Badge>
                    <Ticket className="text-[#C7A45B]" size={20} />
                  </div>

                  <div className="pt-1">
                    <span className="text-[11px] font-bold text-[#6B7280] uppercase tracking-wider block">Mã giảm giá</span>
                    <h3 className="text-2xl font-black text-[#0F6B4F] tracking-tight">{v.code}</h3>
                  </div>

                  <p className="text-xs text-[#6B7280]">
                    Áp dụng cho đơn từ: <strong className="text-[#1F2937]">{v.minSpend.toLocaleString('vi-VN')}đ</strong>
                  </p>
                </div>

                <div className="pt-4 border-t border-[#E8E1D7] flex items-center justify-between gap-3">
                  <span className="text-[11px] text-[#6B7280]">Hạn dùng: {v.expiry}</span>
                  <Link href="/menu">
                    <Button size="sm" className="h-9 px-4 bg-[#C7A45B] hover:bg-[#B89344] text-white font-bold text-xs rounded-xl gap-1 shadow">
                      Dùng ưu đãi ngay
                      <ArrowRight size={14} />
                    </Button>
                  </Link>
                </div>
              </CardContent>
            </Card>
          ))}
        </div>

      </div>
    </section>
  );
}
