'use client';

import React from 'react';
import { Card, CardContent } from '@/components/ui/card';
import { Star, CheckCircle2, MessageSquareQuote } from 'lucide-react';

export function ReviewSection() {
  const reviews = [
    {
      name: 'Nguyễn H*** N***',
      role: 'Thực khách thân thiết',
      text: 'Nước dùng phở ở đây rất thanh ngọt tự nhiên, ninh từ xương ống chứ không bị vị ngọt của mì chính. Thịt bò tái mềm ngon, bánh phở dai đúng chuẩn vị phở Hà Nội cổ.',
      stars: 5,
      date: 'Hôm qua',
      initials: 'NH',
    },
    {
      name: 'Phạm T*** T***',
      role: 'Đặt phở giao tận nơi',
      text: 'Tôi rất thích dịch vụ giao phở tận nhà. Hộp giữ nhiệt chuyên dụng nên phở giao đến vẫn nóng hổi, nước dùng đóng túi riêng rất sạch sẽ và thơm nức.',
      stars: 5,
      date: '3 ngày trước',
      initials: 'PT',
    },
    {
      name: 'Lê M*** T***',
      role: 'Thực khách tại quán',
      text: 'Quán ăn sạch sẽ, thoáng mát. Bát phở đặc biệt nhiều thịt bò, quẩy giòn rụm và trứng chần rất béo béo ngậy. Sẽ tiếp tục quay lại ủng hộ thường xuyên!',
      stars: 5,
      date: '1 tuần trước',
      initials: 'LM',
    },
  ];

  return (
    <section id="reviews" className="py-20 bg-[#FFF9F0] border-b border-[#E8E1D7]">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        
        {/* Header */}
        <div className="text-center max-w-2xl mx-auto mb-16 space-y-3">
          <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-[#FAF0D1] text-[#5E4A22] text-xs font-bold uppercase tracking-wider border border-[#C7A45B]/40">
            <MessageSquareQuote size={14} className="text-[#C7A45B]" />
            ĐÁNH GIÁ CHÂN THỰC
          </div>
          <h2 className="text-3xl sm:text-4xl font-extrabold text-[#1F2937] tracking-tight">
            Khách Hàng <span className="text-[#0F6B4F]">Nói Gì Về Chúng Tôi</span>
          </h2>
          <p className="text-[#6B7280] text-base leading-relaxed">
            Lắng nghe những cảm nhận và trải nghiệm ẩm thực thực tế từ đông đảo thực khách.
          </p>
        </div>

        {/* 3 Reviews Grid */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
          {reviews.map((rev, index) => (
            <Card 
              key={index}
              className="rounded-2xl bg-white border border-[#E8E1D7] hover:border-[#0F6B4F] shadow-sm hover:shadow-lg transition-all duration-300 flex flex-col justify-between"
            >
              <CardContent className="p-8 space-y-5 flex flex-col justify-between h-full">
                <div className="space-y-4">
                  {/* Star rating & verified badge */}
                  <div className="flex items-center justify-between">
                    <div className="flex items-center gap-1 text-amber-400">
                      {Array.from({ length: rev.stars }).map((_, i) => (
                        <Star key={i} size={16} fill="currentColor" />
                      ))}
                    </div>
                    <span className="text-[10px] text-[#6B7280] font-semibold">{rev.date}</span>
                  </div>

                  {/* Review Text Body */}
                  <p className="text-xs sm:text-sm text-[#1F2937] italic leading-relaxed">
                    &ldquo;{rev.text}&rdquo;
                  </p>
                </div>

                {/* Author Info */}
                <div className="pt-5 border-t border-[#E8E1D7] flex items-center justify-between">
                  <div className="flex items-center gap-3">
                    <div className="w-10 h-10 rounded-full bg-[#0F6B4F] text-white font-bold text-xs flex items-center justify-center shadow">
                      {rev.initials}
                    </div>
                    <div>
                      <h4 className="text-xs font-bold text-[#1F2937] flex items-center gap-1">
                        {rev.name}
                        <CheckCircle2 size={14} className="text-[#0F6B4F]" />
                      </h4>
                      <p className="text-[11px] text-[#6B7280]">{rev.role}</p>
                    </div>
                  </div>
                </div>
              </CardContent>
            </Card>
          ))}
        </div>

      </div>
    </section>
  );
}
