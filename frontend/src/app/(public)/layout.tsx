'use client';

import React from 'react';
import { Header } from '@/components/home/Header';
import { Footer } from '@/components/home/Footer';

export default function PublicLayout({ children }: { children: React.ReactNode }) {
  return (
    <div className="flex flex-col min-h-screen bg-[#FFF9F0] text-[#1F2937]">
      {/* Redesigned Sticky Public Header */}
      <Header />

      {/* Main Content Body */}
      <main className="flex-1">
        {children}
      </main>

      {/* Redesigned Dark Green Public Footer */}
      <Footer />
    </div>
  );
}
