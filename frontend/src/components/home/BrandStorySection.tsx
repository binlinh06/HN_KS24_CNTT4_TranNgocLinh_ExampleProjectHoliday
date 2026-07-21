'use client';

import React from 'react';
import Image from 'next/image';
import { Badge } from '@/components/ui/badge';
import { Flame, Clock, Heart, Award } from 'lucide-react';

export function BrandStorySection() {
  const metrics = [
    {
      stat: '24 Giờ',
      label: 'Ninh hầm nước dùng',
      desc: 'Xương ống ninh chậm thảo mộc',
      icon: Clock,
    },
    {
      stat: '100%',
      label: 'Nguyên liệu tươi mới',
      desc: 'Bò tươi tuyển chọn mỗi sáng',
      icon: Flame,
    },
    {
      stat: '500+',
      label: 'Khách hàng/Ngày',
      desc: 'Phục vụ tận tâm chu đáo',
      icon: Heart,
    },
    {
      stat: '4.9 / 5★',
      label: 'Đánh giá yêu thích',
      desc: 'Chuẩn vị truyền thống Hà Nội',
      icon: Award,
    },
  ];

  return (
    <section className="py-20 bg-[#FFFCF7] border-b border-[#E8E1D7]">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-12 lg:gap-16 items-center">
          
          {/* Left Column: Image with accent frame */}
          <div className="lg:col-span-6 relative">
            <div className="relative aspect-[4/3] w-full rounded-3xl overflow-hidden border border-[#E8E1D7] shadow-xl shadow-[#0F6B4F]/10">
              <Image
                src="/images/brand_story_kitchen.png"
                alt="Nồi nước dùng phở hầm gia truyền"
                fill
                className="object-cover hover:scale-105 transition-transform duration-700"
                sizes="(max-width: 1024px) 100vw, 50vw"
              />
              <div className="absolute inset-0 bg-gradient-to-t from-black/50 via-transparent to-transparent" />
              
              <div className="absolute bottom-6 left-6 right-6 text-white bg-black/40 backdrop-blur-md p-4 rounded-2xl border border-white/20">
                <span className="text-xs text-amber-300 font-bold uppercase tracking-wider block mb-1">
                  BÍ QUYẾT GIA TRUYỀN 3 THẾ HỆ
                </span>
                <p className="text-sm font-medium leading-snug">
                  Nước dùng ninh chiết xuất từ xương ống bò hảo hạng hòa quyện cùng 12 vị thảo mộc thiên nhiên.
                </p>
              </div>
            </div>

            {/* Decorative Gold Accent Badge */}
            <div className="absolute -top-4 -left-4 bg-[#C7A45B] text-white p-3.5 rounded-2xl shadow-lg font-bold text-xs flex items-center gap-2 border border-white/40">
              <Award size={18} />
              <span>Thương Hiệu Uy Tín</span>
            </div>
          </div>

          {/* Right Column: Narrative & Metrics */}
          <div className="lg:col-span-6 space-y-6">
            <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-[#ECFDF5] text-[#0F6B4F] text-xs font-bold uppercase tracking-wider border border-[#A7F3D0]">
              CÂU CHUYỆN THƯƠNG HIỆU
            </div>

            <h2 className="text-3xl sm:text-4xl font-extrabold text-[#1F2937] tracking-tight leading-tight">
              Nước Dùng Hầm Chậm <br />
              <span className="text-[#0F6B4F]">Hương Vị Lưu Giữ</span> Theo Năm Tháng
            </h2>

            <p className="text-[#6B7280] text-sm sm:text-base leading-relaxed">
              Phở Bò Gia Truyền tự hào lưu giữ trọn vẹn công thức nấu phở truyền thống Hà Nội xưa. 
              Mỗi buổi sáng, nồi nước dùng thanh ngọt được ninh liên tục trong 24 giờ từ xương ống bò tươi, 
              kết hợp quế, hoa hồi, thảo quả và hành nướng thơm lừng.
            </p>

            <p className="text-[#6B7280] text-sm sm:text-base leading-relaxed">
              Chúng tôi cam kết 100% sử dụng thịt bò tươi VietGAP tuyển chọn cùng bánh phở làm thủ công 
              không chất bảo quản, mang đến cho thực khách bát phở thơm ngon đậm đà và an tâm nhất.
            </p>

            {/* 4 Metrics Grid */}
            <div className="pt-4 grid grid-cols-2 gap-4">
              {metrics.map((m, i) => {
                const Icon = m.icon;
                return (
                  <div key={i} className="p-4 rounded-2xl bg-[#FFF9F0] border border-[#E8E1D7] hover:border-[#0F6B4F] transition-colors">
                    <div className="flex items-center gap-2 mb-1">
                      <Icon size={16} className="text-[#0F6B4F]" />
                      <span className="text-xl font-black text-[#0F6B4F]">{m.stat}</span>
                    </div>
                    <h4 className="text-xs font-bold text-[#1F2937]">{m.label}</h4>
                    <p className="text-[11px] text-[#6B7280] mt-0.5">{m.desc}</p>
                  </div>
                );
              })}
            </div>
          </div>

        </div>
      </div>
    </section>
  );
}
