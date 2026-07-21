'use client';

import React from 'react';
import { Card, CardContent } from '@/components/ui/card';
import { ShoppingBag, CreditCard, Smile } from 'lucide-react';

export function OrderProcessSection() {
  const steps = [
    {
      num: '01',
      title: 'Chọn món yêu thích',
      desc: 'Duyệt thực đơn phở bò đa dạng với các loại tái, nạm, gầu, bò viên & món ăn kèm hấp dẫn.',
      icon: ShoppingBag,
    },
    {
      num: '02',
      title: 'Tùy chỉnh & thanh toán',
      desc: 'Thêm yêu cầu quẩy giòn, trứng chần, lựa chọn phương thức thanh toán COD hoặc Online tiện lợi.',
      icon: CreditCard,
    },
    {
      num: '03',
      title: 'Nhận phở nóng hổi',
      desc: 'Đội ngũ giao hàng đưa bát phở nóng hổi, thơm ngon cùng bao bì giữ nhiệt cao cấp tận tay bạn trong 30 phút.',
      icon: Smile,
    },
  ];

  return (
    <section className="py-20 bg-[#FFFCF7] border-b border-[#E8E1D7]">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        
        {/* Header */}
        <div className="text-center max-w-2xl mx-auto mb-16 space-y-3">
          <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-[#ECFDF5] text-[#0F6B4F] text-xs font-bold uppercase tracking-wider border border-[#A7F3D0]">
            QUY TRÌNH ĐƠN GIẢN
          </div>
          <h2 className="text-3xl sm:text-4xl font-extrabold text-[#1F2937] tracking-tight">
            Đặt Món Chỉ Trong <span className="text-[#0F6B4F]">3 Bước Nhanh Chóng</span>
          </h2>
          <p className="text-[#6B7280] text-base leading-relaxed">
            Trải nghiệm quy trình đặt phở bò gia truyền trực tuyến dễ dàng và nhận hàng tận nhà nhanh chóng.
          </p>
        </div>

        {/* 3 Steps Timeline Grid */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-8 relative">
          
          {/* Connector Line for Desktop */}
          <div className="hidden md:block absolute top-1/2 left-[15%] right-[15%] h-0.5 bg-dashed border-t-2 border-dashed border-[#C7A45B]/40 -translate-y-6 pointer-events-none z-0" />

          {steps.map((s, i) => {
            const Icon = s.icon;
            return (
              <Card 
                key={i} 
                className="relative z-10 rounded-2xl bg-white border border-[#E8E1D7] hover:border-[#0F6B4F] shadow-sm hover:shadow-md transition-all duration-300 overflow-hidden"
              >
                <CardContent className="p-8 relative space-y-4">
                  {/* Step Number Tag */}
                  <div className="flex items-center justify-between">
                    <div className="w-12 h-12 rounded-xl bg-[#0F6B4F] text-white flex items-center justify-center font-extrabold shadow-md shadow-[#0F6B4F]/20">
                      <Icon size={22} />
                    </div>
                    <span className="text-4xl font-black text-[#C7A45B]/30 tracking-tight">
                      {s.num}
                    </span>
                  </div>

                  <h3 className="text-lg font-bold text-[#1F2937] pt-2">
                    {s.title}
                  </h3>
                  
                  <p className="text-xs text-[#6B7280] leading-relaxed">
                    {s.desc}
                  </p>
                </CardContent>
              </Card>
            );
          })}

        </div>

      </div>
    </section>
  );
}
