'use client';

import React from 'react';
import Link from 'next/link';
import { Button } from '@/components/ui/button';
import { Card, CardContent } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { mockMenuItems, mockVouchers } from '@/mocks/dashboard';
import { 
  ArrowRight, 
  CheckCircle, 
  ShoppingBag, 
  Utensils, 
  Clock, 
  ShieldCheck, 
  ThumbsUp, 
  Star,
  ChefHat
} from 'lucide-react';

export default function PublicHomepage() {
  const bestSellers = mockMenuItems.slice(0, 3);

  const categories = [
    { name: 'Phở Bò', count: '10 món', description: 'Nước dùng hầm xương 24h đặc trưng', icon: ChefHat },
    { name: 'Món ăn kèm', count: '5 món', description: 'Quẩy giòn nóng hổi, trứng chần bổ dưỡng', icon: Utensils },
    { name: 'Thức uống', count: '8 món', description: 'Trà đá Hà Nội, sữa đậu nành mát lạnh', icon: Clock },
  ];

  const reasons = [
    {
      title: 'Bí quyết gia truyền',
      desc: 'Nước dùng thanh ngọt tự nhiên ninh từ xương ống bò kèm sá sùng và 12 loại thảo mộc.',
      icon: ChefHat,
    },
    {
      title: 'Nguyên liệu sạch 100%',
      desc: 'Thịt bò tươi tuyển chọn chuẩn VietGAP mỗi sáng sớm, bánh phở không chứa chất bảo quản.',
      icon: ShieldCheck,
    },
    {
      title: 'Phục vụ tận tâm',
      desc: 'Cam kết mang đến trải nghiệm hài lòng nhất cho quý khách từ trực tiếp tại quán đến giao hàng.',
      icon: ThumbsUp,
    },
  ];

  const steps = [
    { step: '01', title: 'Chọn món', desc: 'Duyệt thực đơn phở bò phong phú và chọn các món ăn kèm hấp dẫn.' },
    { step: '02', title: 'Thanh toán', desc: 'Chọn phương thức thanh toán linh hoạt qua tiền mặt, chuyển khoản hoặc VNPay.' },
    { step: '03', title: 'Giao hàng', desc: 'Đội ngũ giao hàng chuyên nghiệp đưa phở nóng hổi tới tận tay bạn trong 30 phút.' },
  ];

  const reviews = [
    {
      name: 'Nguyễn Hoàng Nam',
      role: 'Ẩm thực viên',
      text: 'Nước dùng phở ở đây rất thanh, không bị ngọt bột ngọt. Thịt bò tái mềm, bánh phở mỏng dai đúng chuẩn phở Hà Nội xưa.',
      stars: 5,
    },
    {
      name: 'Phạm Thu Trang',
      role: 'Khách hàng thân thiết',
      text: 'Tôi rất thích dịch vụ đặt phở giao tận nơi của quán. Hộp đựng giữ nhiệt tốt, phở mang đến vẫn nóng hổi, nước dùng đựng túi riêng sạch sẽ.',
      stars: 5,
    },
    {
      name: 'Lê Minh Triết',
      role: 'Khách hàng',
      text: 'Không gian quán sạch sẽ, nhân viên phục vụ nhanh chóng. Phở đặc biệt siêu nhiều thịt, ăn đáng đồng tiền bát gạo.',
      stars: 4,
    },
  ];

  return (
    <div className="flex flex-col min-h-screen">
      {/* 1. Hero Section */}
      <section className="relative bg-stone-900 text-white overflow-hidden py-24 sm:py-32">
        <div className="absolute inset-0 bg-[radial-gradient(#0F6B4F_1px,transparent_1px)] [background-size:16px_16px] opacity-20" />
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 relative z-10">
          <div className="max-w-2xl">
            <Badge variant="accent" className="mb-4 py-1 px-3 text-xs tracking-wider uppercase font-bold text-accent-50">
              Tinh hoa ẩm thực Việt
            </Badge>
            <h1 className="text-4xl sm:text-5xl lg:text-6xl font-bold tracking-tight text-white leading-tight">
              Hương vị Phở Bò <br />
              <span className="text-accent-500">Gia Truyền</span> Chuẩn Vị
            </h1>
            <p className="mt-6 text-base sm:text-lg text-stone-300 leading-relaxed">
              Trải nghiệm bát phở nóng hổi với nước dùng ngọt thanh ninh từ xương ống suốt 24 giờ, kết hợp cùng bánh phở dai mềm và thịt bò tươi ngon tuyển chọn trong ngày.
            </p>
            <div className="mt-10 flex flex-wrap gap-4">
              <Link href="/login">
                <Button size="lg" className="bg-primary-500 hover:bg-primary-600 gap-2 font-bold">
                  Đặt mua ngay
                  <ShoppingBag size={18} />
                </Button>
              </Link>
              <Link href="#categories">
                <Button size="lg" variant="outline" className="border-stone-750 text-white hover:bg-stone-850 gap-2">
                  Xem thực đơn
                  <ArrowRight size={18} />
                </Button>
              </Link>
            </div>
          </div>
        </div>
      </section>

      {/* 2. Highlighted Categories */}
      <section id="categories" className="py-20 bg-white border-b border-stone-200">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center max-w-xl mx-auto mb-16">
            <h2 className="text-3xl font-bold text-stone-900 tracking-tight">Danh Mục Thực Đơn</h2>
            <p className="mt-3 text-stone-500 text-sm">
              Khám phá thực đơn đa dạng được chăm chút kỹ lưỡng bởi những đầu bếp giàu kinh nghiệm
            </p>
          </div>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
            {categories.map((cat, index) => {
              const Icon = cat.icon;
              return (
                <Card key={index} className="hover:border-primary-500/30 hover:shadow-md transition-all duration-300">
                  <CardContent className="p-8">
                    <div className="w-12 h-12 rounded-xl bg-primary-50 flex items-center justify-center text-primary-600 mb-6 border border-primary-100">
                      <Icon size={22} />
                    </div>
                    <h3 className="text-lg font-bold text-stone-900">{cat.name}</h3>
                    <Badge variant="neutral" className="mt-2 text-[10px]">
                      {cat.count}
                    </Badge>
                    <p className="mt-4 text-stone-500 text-sm leading-relaxed">
                      {cat.description}
                    </p>
                  </CardContent>
                </Card>
              );
            })}
          </div>
        </div>
      </section>

      {/* 3. Best Sellers */}
      <section id="best-sellers" className="py-20 bg-stone-50 border-b border-stone-200">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center max-w-xl mx-auto mb-16">
            <h2 className="text-3xl font-bold text-stone-900 tracking-tight">Món Ăn Bán Chạy</h2>
            <p className="mt-3 text-stone-500 text-sm">
              Những món phở đậm đà được khách hàng ưa chuộng và lựa chọn nhiều nhất tại hệ thống
            </p>
          </div>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
            {bestSellers.map((item) => (
              <Card key={item.id} className="overflow-hidden hover:shadow-md transition-all duration-300">
                <CardContent className="p-0">
                  <div className="aspect-video w-full bg-stone-200 relative flex items-center justify-center text-stone-450 border-b border-stone-100">
                    <Utensils size={32} className="text-stone-300" />
                  </div>
                  <div className="p-6">
                    <div className="flex justify-between items-start">
                      <h3 className="text-lg font-bold text-stone-900 leading-tight">{item.name}</h3>
                      <Badge variant="primary" className="text-[10px]">
                        Bán chạy
                      </Badge>
                    </div>
                    <p className="text-xs text-stone-500 mt-1">{item.category}</p>
                    <div className="mt-6 flex items-center justify-between">
                      <span className="text-lg font-bold text-primary-600">
                        {item.price.toLocaleString('vi-VN')}đ
                      </span>
                      <Link href="/login">
                        <Button size="sm" className="bg-primary-500 hover:bg-primary-600 text-xs">
                          Đặt mua
                        </Button>
                      </Link>
                    </div>
                  </div>
                </CardContent>
              </Card>
            ))}
          </div>
        </div>
      </section>

      {/* 4. Why Choose Us */}
      <section className="py-20 bg-white border-b border-stone-200">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center max-w-xl mx-auto mb-16">
            <h2 className="text-3xl font-bold text-stone-900 tracking-tight">Tại Sao Chọn Chúng Tôi</h2>
            <p className="mt-3 text-stone-500 text-sm">
              Chúng tôi luôn đặt chất lượng sản phẩm và sự an tâm của khách hàng lên hàng đầu
            </p>
          </div>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
            {reasons.map((r, index) => {
              const Icon = r.icon;
              return (
                <div key={index} className="text-center p-6">
                  <div className="mx-auto w-14 h-14 rounded-full bg-primary-50 flex items-center justify-center text-primary-600 mb-6 border border-primary-100">
                    <Icon size={24} />
                  </div>
                  <h3 className="text-lg font-bold text-stone-900 mb-3">{r.title}</h3>
                  <p className="text-stone-500 text-sm leading-relaxed">{r.desc}</p>
                </div>
              );
            })}
          </div>
        </div>
      </section>

      {/* 5. Ordering Workflow */}
      <section className="py-20 bg-stone-50 border-b border-stone-200">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center max-w-xl mx-auto mb-16">
            <h2 className="text-3xl font-bold text-stone-900 tracking-tight">Quy Trình Đặt Món</h2>
            <p className="mt-3 text-stone-500 text-sm">
              Chỉ với 3 bước cực kỳ đơn giản và nhanh chóng để có ngay tô phở nóng hổi
            </p>
          </div>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-8 relative">
            {steps.map((s, index) => (
              <Card key={index} className="relative z-10">
                <CardContent className="p-8">
                  <span className="absolute right-6 top-6 text-4xl font-extrabold text-stone-200">
                    {s.step}
                  </span>
                  <h3 className="text-lg font-bold text-stone-900 mb-3 mt-2">{s.title}</h3>
                  <p className="text-stone-500 text-sm leading-relaxed">{s.desc}</p>
                </CardContent>
              </Card>
            ))}
          </div>
        </div>
      </section>

      {/* 6. Hot Promotions */}
      <section className="py-20 bg-white border-b border-stone-200">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center max-w-xl mx-auto mb-16">
            <h2 className="text-3xl font-bold text-stone-900 tracking-tight">Chương Trình Khuyến Mãi</h2>
            <p className="mt-3 text-stone-500 text-sm">
              Đăng ký thành viên để áp dụng các mã giảm giá đặc biệt khi thanh toán đơn hàng
            </p>
          </div>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
            {mockVouchers.map((v, index) => (
              <Card key={index} className="border-dashed border-2 border-stone-300 hover:border-primary-500 transition-all">
                <CardContent className="p-8 flex flex-col justify-between h-full">
                  <div>
                    <Badge variant="accent" className="mb-4">
                      VOUCHER {v.discount}
                    </Badge>
                    <h3 className="text-xl font-bold text-stone-900 mb-2">{v.code}</h3>
                    <p className="text-xs text-stone-500">
                      Đơn hàng tối thiểu: {v.minSpend.toLocaleString('vi-VN')}đ
                    </p>
                  </div>
                  <div className="mt-6 pt-4 border-t border-stone-100 flex items-center justify-between">
                    <span className="text-[10px] text-stone-400 font-semibold">Hạn dùng: {v.expiry}</span>
                    <Link href="/login">
                      <Button size="sm" variant="ghost" className="text-primary-600 hover:text-primary-750 font-bold p-0 text-xs">
                        Sử dụng ngay →
                      </Button>
                    </Link>
                  </div>
                </CardContent>
              </Card>
            ))}
          </div>
        </div>
      </section>

      {/* 7. Customer Reviews */}
      <section id="reviews" className="py-20 bg-stone-50 border-b border-stone-200">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center max-w-xl mx-auto mb-16">
            <h2 className="text-3xl font-bold text-stone-900 tracking-tight">Khách Hàng Nói Gì</h2>
            <p className="mt-3 text-stone-500 text-sm">
              Lắng nghe phản hồi chân thực từ những khách hàng đã trải nghiệm phở tại quán
            </p>
          </div>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
            {reviews.map((rev, index) => (
              <Card key={index}>
                <CardContent className="p-8">
                  <div className="flex items-center gap-1 text-amber-400 mb-4">
                    {Array.from({ length: rev.stars }).map((_, i) => (
                      <Star key={i} size={16} fill="currentColor" />
                    ))}
                  </div>
                  <p className="text-stone-600 text-sm italic leading-relaxed">
                    "{rev.text}"
                  </p>
                  <div className="mt-6 pt-6 border-t border-stone-150 flex items-center justify-between">
                    <div>
                      <h4 className="text-sm font-bold text-stone-900">{rev.name}</h4>
                      <p className="text-[10px] text-stone-400 font-semibold">{rev.role}</p>
                    </div>
                    <CheckCircle size={18} className="text-primary-500" />
                  </div>
                </CardContent>
              </Card>
            ))}
          </div>
        </div>
      </section>

      {/* 8. Call To Action (CTA) */}
      <section className="py-20 bg-primary-700 text-white relative overflow-hidden">
        <div className="absolute inset-0 bg-[radial-gradient(#C7A45B_1px,transparent_1px)] [background-size:24px_24px] opacity-15" />
        <div className="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8 text-center relative z-10">
          <h2 className="text-3xl sm:text-4xl font-bold tracking-tight">Thưởng Thức Phở Bò Gia Truyền Ngay Hôm Nay!</h2>
          <p className="mt-6 text-base text-primary-100 max-w-xl mx-auto leading-relaxed">
            Đăng ký tài khoản thành viên chỉ trong 1 phút để nhận voucher giảm giá 10% cho đơn hàng đầu tiên của bạn.
          </p>
          <div className="mt-10 flex flex-wrap justify-center gap-4">
            <Link href="/register">
              <Button size="lg" className="bg-accent-500 hover:bg-accent-600 text-white font-bold gap-2">
                Đăng ký thành viên
                <ArrowRight size={18} />
              </Button>
            </Link>
            <Link href="/login">
              <Button size="lg" variant="outline" className="border-primary-600 text-white hover:bg-primary-600 gap-2">
                Đăng nhập hệ thống
              </Button>
            </Link>
          </div>
        </div>
      </section>
    </div>
  );
}
