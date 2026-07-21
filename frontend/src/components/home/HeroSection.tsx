'use client';

import React from 'react';
import Link from 'next/link';
import Image from 'next/image';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { ShoppingBag, ArrowRight, Star, Users, Flame, Clock, ShieldCheck, Truck } from 'lucide-react';

export function HeroSection() {
  return (
    <section className="relative bg-[#FFF9F0] border-b border-[#E8E1D7] overflow-hidden py-12 lg:py-20">
      {/* Background Decorative Pattern */}
      <div className="absolute inset-0 bg-[radial-gradient(#0F6B4F_0.75px,transparent_0.75px)] [background-size:24px_24px] opacity-10 pointer-events-none" />
      
      {/* Organic Background Shape behind right column */}
      <div className="absolute right-0 top-1/2 -translate-y-1/2 w-[600px] h-[600px] bg-gradient-to-br from-[#0F6B4F]/10 via-[#C7A45B]/15 to-transparent rounded-full blur-3xl pointer-events-none -mr-48" />

      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 relative z-10">
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-12 lg:gap-8 items-center min-h-[580px]">
          
          {/* Left Column: Copy & CTAs */}
          <div className="lg:col-span-7 space-y-6 text-left">
            <div className="inline-flex items-center gap-2 px-3.5 py-1.5 rounded-full bg-[#FAF0D1] border border-[#C7A45B]/40 text-[#5E4A22] text-xs font-bold uppercase tracking-wider">
              <span className="w-2 h-2 rounded-full bg-[#C7A45B] animate-pulse" />
              TINH HOA ẨM THỰC VIỆT
            </div>

            <h1 className="text-4xl sm:text-5xl lg:text-6xl font-extrabold text-[#1F2937] tracking-tight leading-[1.15]">
              Phở Bò Gia Truyền <br />
              <span className="text-[#0F6B4F]">Đậm Đà Từng Giọt</span>{' '}
              <span className="text-[#C7A45B] relative inline-block">
                Nước Dùng
                <svg className="absolute -bottom-2 left-0 w-full h-3 text-[#C7A45B]/40" viewBox="0 0 100 20" preserveAspectRatio="none">
                  <path d="M0 15 Q50 0 100 15" stroke="currentColor" strokeWidth="4" fill="none" />
                </svg>
              </span>
            </h1>

            <p className="text-base sm:text-lg text-[#6B7280] leading-relaxed max-w-2xl font-medium">
              Trải nghiệm bát phở nóng hổi với nước dùng ngọt thanh ninh từ xương ống suốt 24 giờ, 
              kết hợp cùng bánh phở dai mềm và thịt bò tươi ngon tuyển chọn trong ngày.
            </p>

            {/* CTAs */}
            <div className="pt-2 flex flex-wrap gap-4 items-center">
              <Link href="/menu">
                <Button size="lg" className="h-13 px-8 bg-[#0F6B4F] hover:bg-[#084C38] text-white font-bold text-base rounded-2xl shadow-lg shadow-[#0F6B4F]/20 gap-2.5 transition-all duration-300 hover:scale-[1.02]">
                  <ShoppingBag size={20} />
                  Đặt món ngay
                </Button>
              </Link>
              <Link href="/#categories">
                <Button size="lg" variant="outline" className="h-13 px-7 border-[#E8E1D7] text-[#1F2937] hover:bg-[#FFFCF7] hover:border-[#0F6B4F] font-bold text-base rounded-2xl gap-2 transition-all">
                  Xem thực đơn
                  <ArrowRight size={18} className="text-[#0F6B4F]" />
                </Button>
              </Link>
            </div>

            {/* Trust Badges Bar */}
            <div className="pt-6 border-t border-[#E8E1D7] grid grid-cols-1 sm:grid-cols-3 gap-4">
              <div className="flex items-center gap-3">
                <div className="w-10 h-10 rounded-xl bg-[#0F6B4F]/10 text-[#0F6B4F] flex items-center justify-center font-bold shrink-0">
                  <Clock size={20} />
                </div>
                <div>
                  <h4 className="text-xs font-bold text-[#1F2937]">Hầm chậm 24 giờ</h4>
                  <p className="text-[11px] text-[#6B7280]">Nước dùng ngọt thanh</p>
                </div>
              </div>

              <div className="flex items-center gap-3">
                <div className="w-10 h-10 rounded-xl bg-[#C7A45B]/15 text-[#5E4A22] flex items-center justify-center font-bold shrink-0">
                  <ShieldCheck size={20} />
                </div>
                <div>
                  <h4 className="text-xs font-bold text-[#1F2937]">100% Thịt bò tươi</h4>
                  <p className="text-[11px] text-[#6B7280]">Đạt chuẩn VietGAP</p>
                </div>
              </div>

              <div className="flex items-center gap-3">
                <div className="w-10 h-10 rounded-xl bg-[#0F6B4F]/10 text-[#0F6B4F] flex items-center justify-center font-bold shrink-0">
                  <Truck size={20} />
                </div>
                <div>
                  <h4 className="text-xs font-bold text-[#1F2937]">Giao nhanh 30 phút</h4>
                  <p className="text-[11px] text-[#6B7280]">Đảm bảo nóng hổi</p>
                </div>
              </div>
            </div>
          </div>

          {/* Right Column: Featured Image & Floating Badges */}
          <div className="lg:col-span-5 relative flex justify-center items-center">
            {/* Soft backdrop circle */}
            <div className="relative w-full max-w-[460px] aspect-square rounded-[2.5rem] bg-gradient-to-tr from-[#0F6B4F] to-[#C7A45B] p-1.5 shadow-2xl shadow-[#0F6B4F]/20">
              <div className="w-full h-full rounded-[2.4rem] overflow-hidden relative bg-[#FFFCF7]">
                <Image
                  src="/images/hero_pho_bowl.png"
                  alt="Bát Phở Bò Gia Truyền"
                  fill
                  priority
                  className="object-cover hover:scale-105 transition-transform duration-700"
                  sizes="(max-width: 768px) 100vw, 500px"
                />
                
                {/* Subtle Steam indicator */}
                <div className="absolute top-4 left-4 bg-black/40 backdrop-blur-md text-white text-[11px] font-semibold px-3 py-1 rounded-full flex items-center gap-1.5">
                  <span className="w-2 h-2 rounded-full bg-emerald-400 animate-ping" />
                  Phở nóng hổi mỗi giờ
                </div>
              </div>

              {/* Floating Badge 1: Rating (Top Right) */}
              <div className="absolute -top-4 -right-4 bg-white/95 backdrop-blur-md border border-[#E8E1D7] rounded-2xl p-3.5 shadow-xl flex items-center gap-3 animate-slide-up">
                <div className="w-10 h-10 rounded-xl bg-amber-50 text-amber-500 flex items-center justify-center shrink-0">
                  <Star size={22} fill="currentColor" />
                </div>
                <div>
                  <div className="flex items-center gap-1 text-sm font-extrabold text-[#1F2937]">
                    4.9 / 5.0
                  </div>
                  <p className="text-[11px] text-[#6B7280]">500+ Đánh giá 5★</p>
                </div>
              </div>

              {/* Floating Badge 2: Customer count (Bottom Left) */}
              <div className="absolute -bottom-5 -left-4 bg-white/95 backdrop-blur-md border border-[#E8E1D7] rounded-2xl p-3.5 shadow-xl flex items-center gap-3 animate-slide-up">
                <div className="w-10 h-10 rounded-xl bg-[#0F6B4F]/10 text-[#0F6B4F] flex items-center justify-center shrink-0">
                  <Users size={22} />
                </div>
                <div>
                  <div className="text-sm font-extrabold text-[#1F2937]">
                    500+ Khách/Ngày
                  </div>
                  <p className="text-[11px] text-[#6B7280]">Tin tưởng & ủng hộ</p>
                </div>
              </div>

              {/* Floating Badge 3: Best seller tag (Bottom Right) */}
              <div className="absolute -bottom-6 right-8 bg-[#084C38] text-white rounded-2xl px-4 py-2.5 shadow-lg flex items-center gap-2 border border-[#C7A45B]/40">
                <Flame size={18} className="text-[#C7A45B]" />
                <span className="text-xs font-bold tracking-wide">Bán chạy nhất hôm nay</span>
              </div>
            </div>
          </div>

        </div>
      </div>
    </section>
  );
}
