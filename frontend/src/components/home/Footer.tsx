'use client';

import React from 'react';
import Link from 'next/link';
import { Utensils, MapPin, Phone, Mail, Clock, Facebook, Instagram, Youtube } from 'lucide-react';

export function Footer() {
  return (
    <footer className="bg-[#084C38] text-stone-200 border-t border-[#C7A45B]/30 relative overflow-hidden">
      {/* Subtle Background Pattern */}
      <div className="absolute inset-0 bg-[radial-gradient(#C7A45B_0.75px,transparent_0.75px)] [background-size:20px_20px] opacity-10 pointer-events-none" />

      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-16 relative z-10">
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-5 gap-10">
          
          {/* Col 1: Brand Info */}
          <div className="lg:col-span-2 space-y-5">
            <Link href="/" className="flex items-center gap-3">
              <div className="w-10 h-10 rounded-xl bg-[#C7A45B] text-white flex items-center justify-center font-bold shadow-md">
                <Utensils size={20} />
              </div>
              <div>
                <span className="font-extrabold text-white text-base tracking-tight block">PHỞ BÒ GIA TRUYỀN</span>
                <span className="text-[9px] text-[#C7A45B] uppercase tracking-widest block font-bold mt-0.5">
                  Hương vị truyền thống Việt Nam
                </span>
              </div>
            </Link>

            <p className="text-xs leading-relaxed text-stone-300 max-w-sm">
              Mang đến hương vị phở bò gia truyền chuẩn vị Hà Nội cổ xưa. 
              Nước dùng ngọt thanh ninh từ xương ống suốt 24 giờ, kết hợp bánh phở tươi 
              và thịt bò tuyển chọn an toàn vệ sinh mỗi ngày.
            </p>

            {/* Social Icons */}
            <div className="flex items-center gap-3 pt-2">
              <a href="#" className="w-9 h-9 rounded-full bg-white/10 hover:bg-[#C7A45B] text-white flex items-center justify-center transition-colors">
                <Facebook size={16} />
              </a>
              <a href="#" className="w-9 h-9 rounded-full bg-white/10 hover:bg-[#C7A45B] text-white flex items-center justify-center transition-colors">
                <Instagram size={16} />
              </a>
              <a href="#" className="w-9 h-9 rounded-full bg-white/10 hover:bg-[#C7A45B] text-white flex items-center justify-center transition-colors">
                <Youtube size={16} />
              </a>
            </div>
          </div>

          {/* Col 2: Quick Links */}
          <div className="space-y-4">
            <h4 className="text-white font-extrabold text-xs tracking-wider uppercase border-b border-[#C7A45B]/30 pb-2 inline-block">
              LIÊN KẾT NHANH
            </h4>
            <ul className="space-y-2.5 text-xs text-stone-300">
              <li><Link href="/" className="hover:text-[#C7A45B] transition-colors">Trang chủ</Link></li>
              <li><Link href="/menu" className="hover:text-[#C7A45B] transition-colors">Thực đơn phở bò</Link></li>
              <li><Link href="/#categories" className="hover:text-[#C7A45B] transition-colors">Danh mục món ăn</Link></li>
              <li><Link href="/#best-sellers" className="hover:text-[#C7A45B] transition-colors">Món bán chạy</Link></li>
              <li><Link href="/#promotions" className="hover:text-[#C7A45B] transition-colors">Mã giảm giá Voucher</Link></li>
            </ul>
          </div>

          {/* Col 3: Opening Hours */}
          <div className="space-y-4">
            <h4 className="text-white font-extrabold text-xs tracking-wider uppercase border-b border-[#C7A45B]/30 pb-2 inline-block">
              GIỜ MỞ CỬA
            </h4>
            <ul className="space-y-2.5 text-xs text-stone-300">
              <li className="flex items-start gap-2">
                <Clock size={15} className="text-[#C7A45B] shrink-0 mt-0.5" />
                <div>
                  <span className="block font-bold text-white">Thứ 2 - Thứ Sáu</span>
                  <span className="text-stone-400">06:00 - 22:00</span>
                </div>
              </li>
              <li className="flex items-start gap-2">
                <Clock size={15} className="text-[#C7A45B] shrink-0 mt-0.5" />
                <div>
                  <span className="block font-bold text-white">Thứ 7 - Chủ Nhật</span>
                  <span className="text-stone-400">06:00 - 23:00</span>
                </div>
              </li>
              <li className="text-[11px] text-amber-300 font-semibold pt-1">
                ⚡ Giao hàng tận nơi: 06:30 - 21:30
              </li>
            </ul>
          </div>

          {/* Col 4: Store Contact */}
          <div className="space-y-4">
            <h4 className="text-white font-extrabold text-xs tracking-wider uppercase border-b border-[#C7A45B]/30 pb-2 inline-block">
              THÔNG TIN LIÊN HỆ
            </h4>
            <ul className="space-y-3 text-xs text-stone-300">
              <li className="flex items-start gap-2.5">
                <MapPin size={16} className="text-[#C7A45B] shrink-0 mt-0.5" />
                <span>123 Đường Láng, Đống Đa, Hà Nội</span>
              </li>
              <li className="flex items-center gap-2.5">
                <Phone size={16} className="text-[#C7A45B] shrink-0" />
                <span className="font-bold text-white">1900 1234 / 0988 123 456</span>
              </li>
              <li className="flex items-center gap-2.5">
                <Mail size={16} className="text-[#C7A45B] shrink-0" />
                <span>contact@phobogiatruyen.vn</span>
              </li>
            </ul>
          </div>

        </div>

        {/* Bottom Copyright Bar */}
        <div className="border-t border-white/10 mt-12 pt-6 flex flex-col sm:flex-row items-center justify-between gap-4 text-xs text-stone-400">
          <p>© {new Date().getFullYear()} Phở Bò Gia Truyền Management System. All Rights Reserved.</p>
          <div className="flex gap-6 text-[11px]">
            <a href="#" className="hover:text-white transition-colors">Chính sách bảo mật</a>
            <a href="#" className="hover:text-white transition-colors">Điều khoản dịch vụ</a>
            <a href="#" className="hover:text-white transition-colors">Quy định giao hàng</a>
          </div>
        </div>
      </div>
    </footer>
  );
}
