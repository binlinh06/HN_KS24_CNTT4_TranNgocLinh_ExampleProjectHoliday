'use client';

import React from 'react';
import Image from 'next/image';
import Link from 'next/link';
import { Card, CardContent } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { showToast } from '@/components/ui/toast';
import { useFeaturedProducts, useProducts } from '@/features/products/api';
import { useGuestCartStore } from '@/stores/cart';
import { ShoppingCart, Star, Utensils, Flame, ArrowRight } from 'lucide-react';
import { Product } from '@/types';

export function BestSellerSection() {
  const { data: featured = [], isLoading: isLoadingFeatured, isError } = useFeaturedProducts();
  const { data: allProductsData, isLoading: isLoadingAll } = useProducts({ size: 8 });

  // Use featured products first, or fallback to first 8 products from catalog
  const productsList: Product[] = (featured && featured.length > 0)
    ? featured
    : (allProductsData?.products && allProductsData.products.length > 0)
    ? allProductsData.products
    : [];

  const isLoading = isLoadingFeatured || isLoadingAll;

  const handleAddToCart = (product: Product, e: React.MouseEvent) => {
    e.preventDefault();
    e.stopPropagation();

    useGuestCartStore.getState().addItem({
      productId: product.id,
      productName: product.productName,
      basePrice: product.basePrice,
      imageUrl: product.imageUrl || '/images/hero_pho_bowl.png',
      quantity: 1,
      optionIds: [],
      specialNote: '',
    });

    showToast.success(`Đã thêm "${product.productName}" vào giỏ hàng!`);
  };

  return (
    <section id="best-sellers" className="py-20 bg-[#FFF9F0] border-b border-[#E8E1D7]">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        
        {/* Section Header */}
        <div className="flex flex-col md:flex-row md:items-end justify-between mb-14 gap-6">
          <div className="max-w-xl space-y-3">
            <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-[#FDF8EB] text-[#5E4A22] text-xs font-bold uppercase tracking-wider border border-[#C7A45B]/40">
              <Flame size={14} className="text-[#C7A45B]" />
              MÓN NỔI BẬT NHẤT
            </div>
            <h2 className="text-3xl sm:text-4xl font-extrabold text-[#1F2937] tracking-tight">
              Món Ăn <span className="text-[#0F6B4F]">Được Yêu Thích Nhất</span>
            </h2>
            <p className="text-[#6B7280] text-sm sm:text-base leading-relaxed">
              Những bát phở thơm ngon chuẩn vị và món ăn kèm được khách hàng đặt nhiều nhất mỗi ngày.
            </p>
          </div>

          <Link href="/menu" className="shrink-0">
            <Button variant="outline" className="h-11 px-5 border-[#0F6B4F] text-[#0F6B4F] hover:bg-[#0F6B4F] hover:text-white font-bold text-sm rounded-xl gap-2 transition-all">
              Xem toàn bộ thực đơn
              <ArrowRight size={16} />
            </Button>
          </Link>
        </div>

        {/* Loading Skeleton */}
        {isLoading ? (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
            {[1, 2, 3, 4].map((i) => (
              <div key={i} className="h-[360px] bg-white rounded-2xl border border-[#E8E1D7] p-4 flex flex-col justify-between animate-pulse">
                <div className="w-full aspect-[4/3] bg-stone-200 rounded-xl" />
                <div className="space-y-2 mt-4">
                  <div className="h-5 bg-stone-200 rounded w-3/4" />
                  <div className="h-4 bg-stone-200 rounded w-1/2" />
                </div>
                <div className="flex justify-between items-center mt-6">
                  <div className="h-6 bg-stone-200 rounded w-1/3" />
                  <div className="h-9 bg-stone-200 rounded w-1/3" />
                </div>
              </div>
            ))}
          </div>
        ) : isError ? (
          <div className="text-center py-12 bg-white rounded-2xl border border-[#E8E1D7] p-8">
            <p className="text-sm text-[#6B7280]">Không thể tải danh sách món ăn. Vui lòng thử lại sau.</p>
          </div>
        ) : productsList.length === 0 ? (
          <div className="text-center py-12 bg-white rounded-2xl border border-[#E8E1D7] p-8 space-y-3">
            <Utensils size={36} className="mx-auto text-[#0F6B4F]/40" />
            <p className="text-sm font-semibold text-[#1F2937]">Hiện chưa có món ăn nổi bật nào được cập nhật.</p>
          </div>
        ) : (
          /* Products Grid */
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
            {productsList.slice(0, 8).map((product) => {
              const imageSrc = product.imageUrl || '/images/hero_pho_bowl.png';
              
              return (
                <Card 
                  key={product.id} 
                  className="group rounded-2xl bg-white border border-[#E8E1D7] hover:border-[#0F6B4F] shadow-sm hover:shadow-xl transition-all duration-300 flex flex-col justify-between overflow-hidden"
                >
                  <CardContent className="p-0 flex flex-col justify-between h-full">
                    <div>
                      {/* Image Container */}
                      <div className="relative aspect-[4/3] w-full overflow-hidden bg-stone-100 border-b border-[#E8E1D7]">
                        <Image
                          src={imageSrc}
                          alt={product.productName}
                          fill
                          className="object-cover group-hover:scale-105 transition-transform duration-500"
                          sizes="(max-width: 768px) 100vw, (max-width: 1200px) 50vw, 25vw"
                        />
                        <div className="absolute top-3 left-3 flex gap-2">
                          <Badge className="bg-[#0F6B4F] text-white border-none font-bold text-[10px] px-2 py-0.5 shadow">
                            Bán chạy
                          </Badge>
                        </div>
                        <div className="absolute top-3 right-3 bg-white/90 backdrop-blur-md px-2 py-1 rounded-lg border border-[#E8E1D7] shadow-sm flex items-center gap-1 text-[11px] font-bold text-[#1F2937]">
                          <Star size={12} className="text-amber-500 fill-amber-500" />
                          <span>4.9</span>
                        </div>
                      </div>

                      {/* Content Info */}
                      <div className="p-5 space-y-2">
                        <span className="text-[11px] font-bold uppercase tracking-wider text-[#C7A45B]">
                          {product.category?.categoryName || 'Phở Bò'}
                        </span>
                        <h3 className="text-base font-bold text-[#1F2937] leading-snug line-clamp-1 group-hover:text-[#0F6B4F] transition-colors">
                          {product.productName}
                        </h3>
                        <p className="text-xs text-[#6B7280] line-clamp-2 leading-relaxed">
                          {product.description || 'Thịt bò tươi mềm kết hợp nước dùng gia truyền đậm đà ngon ngọt chuẩn vị Hà Nội.'}
                        </p>
                      </div>
                    </div>

                    {/* Price & Add to Cart Footer */}
                    <div className="p-5 pt-0 mt-auto flex items-center justify-between gap-3">
                      <div>
                        <span className="text-xs text-[#6B7280] block">Giá chỉ</span>
                        <span className="text-base font-extrabold text-[#0F6B4F]">
                          {product.basePrice.toLocaleString('vi-VN')}đ
                        </span>
                      </div>

                      <Button
                        size="sm"
                        onClick={(e) => handleAddToCart(product, e)}
                        className="h-10 px-3.5 bg-[#0F6B4F] hover:bg-[#084C38] text-white font-bold text-xs rounded-xl gap-1.5 shadow-sm transition-all"
                      >
                        <ShoppingCart size={15} />
                        Thêm vào giỏ
                      </Button>
                    </div>
                  </CardContent>
                </Card>
              );
            })}
          </div>
        )}

      </div>
    </section>
  );
}
