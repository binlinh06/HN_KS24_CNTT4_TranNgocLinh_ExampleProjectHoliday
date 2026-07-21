'use client';

import React from 'react';
import { HeroSection } from '@/components/home/HeroSection';
import { CategorySection } from '@/components/home/CategorySection';
import { BestSellerSection } from '@/components/home/BestSellerSection';
import { BrandStorySection } from '@/components/home/BrandStorySection';
import { WhyChooseUsSection } from '@/components/home/WhyChooseUsSection';
import { OrderProcessSection } from '@/components/home/OrderProcessSection';
import { PromotionSection } from '@/components/home/PromotionSection';
import { ReviewSection } from '@/components/home/ReviewSection';
import { FinalCtaSection } from '@/components/home/FinalCtaSection';

export default function PublicHomepage() {
  return (
    <div className="flex flex-col min-h-screen">
      {/* 1. Hero Section */}
      <HeroSection />

      {/* 2. Category Section */}
      <CategorySection />

      {/* 3. Best Sellers Section */}
      <BestSellerSection />

      {/* 4. Brand Story Section */}
      <BrandStorySection />

      {/* 5. Why Choose Us Section */}
      <WhyChooseUsSection />

      {/* 6. Order Process Section */}
      <OrderProcessSection />

      {/* 7. Promotion Section */}
      <PromotionSection />

      {/* 8. Customer Reviews Section */}
      <ReviewSection />

      {/* 9. Final CTA Section */}
      <FinalCtaSection />
    </div>
  );
}
