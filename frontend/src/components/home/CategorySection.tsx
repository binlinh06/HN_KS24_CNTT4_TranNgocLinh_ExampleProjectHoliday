'use client';

import React from 'react';
import Link from 'next/link';
import Image from 'next/image';
import { Card } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { ArrowRight, Utensils, Sparkles, Coffee, ChefHat } from 'lucide-react';

export function CategorySection() {
  const categoryCards = [
    {
      id: 'pho-bo',
      name: 'Phở Bò Truyền Thống',
      subtitle: 'Tái, Nạm, Gầu, Gân',
      description: 'Bát phở bò nóng hổi đậm đà hương vị Hà Nội truyền thống với nước dùng ninh 24h.',
      image: '/images/hero_pho_bowl.png',
      badge: '10+ Món phở',
      icon: ChefHat,
    },
    {
      id: 'pho-dac-biet',
      name: 'Phở Đặc Biệt Thập Cẩm',
      subtitle: 'Đủ vị thịt bò tuyển chọn',
      description: 'Tô phở cỡ lớn kết hợp đầy đủ tái, chín, nạm, gầu, bò viên & trứng chần thơm ngon.',
      image: '/images/cat_pho_dac_biet.png',
      badge: 'Bán chạy nhất',
      icon: Sparkles,
    },
    {
      id: 'mon-an-kem',
      name: 'Món Ăn Kèm',
      subtitle: 'Quẩy giòn, Trứng chần',
      description: 'Những món ăn kèm chuẩn vị không thể thiếu để trải nghiệm phở trọn vẹn nhất.',
      image: '/images/cat_mon_an_kem.png',
      badge: 'Chuẩn vị phở',
      icon: Utensils,
    },
    {
      id: 'do-uong',
      name: 'Đồ Uống Giải Khát',
      subtitle: 'Trà đá, Sữa đậu nành',
      description: 'Thức uống tươi mát giải nhiệt thanh lọc cơ thể sau tô phở bò nóng hổi.',
      image: '/images/cat_do_uong.png',
      badge: 'Tươi mát',
      icon: Coffee,
    },
  ];

  return (
    <section id="categories" className="py-20 bg-[#FFFCF7] border-b border-[#E8E1D7]">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        
        {/* Header Title */}
        <div className="text-center max-w-2xl mx-auto mb-16 space-y-3">
          <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-[#ECFDF5] text-[#0F6B4F] text-xs font-bold uppercase tracking-wider border border-[#A7F3D0]">
            THỰC ĐƠN ĐA DẠNG
          </div>
          <h2 className="text-3xl sm:text-4xl font-extrabold text-[#1F2937] tracking-tight">
            Khám Phá <span className="text-[#0F6B4F]">Danh Mục Thực Đơn</span>
          </h2>
          <p className="text-[#6B7280] text-base leading-relaxed">
            Thưởng thức hương vị phở bò gia truyền truyền thống và các món ăn kèm hấp dẫn được chuẩn bị tỉ mỉ mỗi ngày.
          </p>
        </div>

        {/* 4 Cards Grid */}
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-8">
          {categoryCards.map((item) => {
            const Icon = item.icon;
            return (
              <Card 
                key={item.id} 
                className="group relative overflow-hidden rounded-[1.5rem] bg-white border border-[#E8E1D7] hover:border-[#0F6B4F] shadow-sm hover:shadow-xl transition-all duration-300 flex flex-col justify-between"
              >
                <div>
                  {/* Category Image Box */}
                  <div className="relative aspect-[4/3] w-full overflow-hidden bg-stone-100">
                    <Image
                      src={item.image}
                      alt={item.name}
                      fill
                      className="object-cover group-hover:scale-108 transition-transform duration-500"
                      sizes="(max-width: 768px) 100vw, (max-width: 1200px) 50vw, 25vw"
                    />
                    <div className="absolute inset-0 bg-gradient-to-t from-black/60 via-transparent to-transparent opacity-80" />
                    
                    {/* Category Badge */}
                    <div className="absolute top-3 left-3">
                      <Badge className="bg-[#0F6B4F] text-white border-none font-semibold text-[11px] shadow-md px-2.5 py-1">
                        {item.badge}
                      </Badge>
                    </div>

                    {/* Category Title Overlay */}
                    <div className="absolute bottom-3 left-4 right-4 text-white">
                      <span className="text-xs text-amber-300 font-medium tracking-wide block">
                        {item.subtitle}
                      </span>
                      <h3 className="text-lg font-bold leading-snug">
                        {item.name}
                      </h3>
                    </div>
                  </div>

                  {/* Card Content Body */}
                  <div className="p-5 space-y-3">
                    <p className="text-xs text-[#6B7280] leading-relaxed line-clamp-2">
                      {item.description}
                    </p>
                  </div>
                </div>

                {/* Card Action Link */}
                <div className="p-5 pt-0 mt-auto">
                  <Link href="/menu" className="w-full">
                    <div className="w-full h-10 rounded-xl bg-[#FFF9F0] group-hover:bg-[#0F6B4F] text-[#0F6B4F] group-hover:text-white font-bold text-xs flex items-center justify-center gap-2 border border-[#E8E1D7] group-hover:border-[#0F6B4F] transition-all duration-300">
                      <span>Xem danh mục</span>
                      <ArrowRight size={14} className="group-hover:translate-x-1 transition-transform" />
                    </div>
                  </Link>
                </div>
              </Card>
            );
          })}
        </div>

      </div>
    </section>
  );
}
