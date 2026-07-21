'use client';

import React from 'react';
import { Card, CardContent } from '@/components/ui/card';
import { ShieldCheck, Flame, Sparkles, Truck } from 'lucide-react';

export function WhyChooseUsSection() {
  const reasons = [
    {
      title: 'Nước Dùng Chuẩn Vị',
      desc: 'Ninh hầm chậm 24 giờ từ xương ống bò tươi cùng 12 loại thảo mộc thiên nhiên thơm lừng thanh ngọt.',
      icon: Flame,
    },
    {
      title: 'Nguyên Liệu Tươi 100%',
      desc: 'Thịt bò tươi ngon đạt chuẩn VietGAP nhập mới mỗi sáng sớm, bánh phở mềm dai không chất bảo quản.',
      icon: ShieldCheck,
    },
    {
      title: 'Chế Biến Sạch Sẽ',
      desc: 'Quy trình gian bếp mở đạt chuẩn an toàn vệ sinh thực phẩm, đảm bảo sức khỏe tuyệt đối cho thực khách.',
      icon: Sparkles,
    },
    {
      title: 'Giao Hàng Nóng Hổi',
      desc: 'Giao phở tận nhà trong 30 phút với bao bì đóng gói giữ nhiệt cao cấp, nước dùng đóng túi riêng tiện lợi.',
      icon: Truck,
    },
  ];

  return (
    <section className="py-20 bg-[#FFF9F0] border-b border-[#E8E1D7]">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        
        {/* Header */}
        <div className="text-center max-w-2xl mx-auto mb-16 space-y-3">
          <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-[#FAF0D1] text-[#5E4A22] text-xs font-bold uppercase tracking-wider border border-[#C7A45B]/40">
            CAM KẾT CHẤT LƯỢNG
          </div>
          <h2 className="text-3xl sm:text-4xl font-extrabold text-[#1F2937] tracking-tight">
            Vì Sao Chọn <span className="text-[#0F6B4F]">Phở Bò Gia Truyền</span>
          </h2>
          <p className="text-[#6B7280] text-base leading-relaxed">
            Chúng tôi luôn giữ gìn sự tận tâm và tỉ mỉ trong từng công đoạn chế biến để mang lại trải nghiệm hoàn hảo.
          </p>
        </div>

        {/* 4 Cards */}
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
          {reasons.map((r, i) => {
            const Icon = r.icon;
            return (
              <Card 
                key={i} 
                className="group rounded-2xl bg-white border border-[#E8E1D7] hover:border-[#0F6B4F] shadow-sm hover:shadow-lg transition-all duration-300 relative overflow-hidden"
              >
                {/* Top Gold Line Indicator */}
                <div className="h-1 w-full bg-[#C7A45B] group-hover:bg-[#0F6B4F] transition-colors" />
                
                <CardContent className="p-6 text-center space-y-4">
                  <div className="mx-auto w-14 h-14 rounded-2xl bg-[#ECFDF5] text-[#0F6B4F] flex items-center justify-center border border-[#A7F3D0] group-hover:scale-110 transition-transform duration-300">
                    <Icon size={26} />
                  </div>
                  <h3 className="text-lg font-bold text-[#1F2937] group-hover:text-[#0F6B4F] transition-colors">
                    {r.title}
                  </h3>
                  <p className="text-xs text-[#6B7280] leading-relaxed">
                    {r.desc}
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
