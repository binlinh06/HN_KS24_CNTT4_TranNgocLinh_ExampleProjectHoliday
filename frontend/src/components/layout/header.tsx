'use client';

import React from 'react';
import { useAuthStore } from '@/stores/auth';
import { Badge } from '@/components/ui/badge';
import { Bell, ChevronRight, User } from 'lucide-react';
import Link from 'next/link';
import { CartBadge } from '@/components/cart/cart-badge';

interface BreadcrumbItem {
  title: string;
  href?: string;
}

interface HeaderProps {
  breadcrumbs: BreadcrumbItem[];
}

export function Header({ breadcrumbs }: HeaderProps) {
  const { user } = useAuthStore();

  return (
    <header className="h-16 bg-white border-b border-stone-200 px-6 sm:px-8 flex items-center justify-between shrink-0">
      {/* Breadcrumbs */}
      <div className="flex items-center gap-1.5 sm:gap-2 text-xs text-stone-500 font-medium pl-10 lg:pl-0">
        {breadcrumbs.map((item, index) => (
          <React.Fragment key={index}>
            {index > 0 && <ChevronRight size={12} className="text-stone-400" />}
            {item.href ? (
              <Link href={item.href} className="hover:text-stone-850 transition-colors">
                {item.title}
              </Link>
            ) : (
              <span className="text-stone-800 font-semibold">{item.title}</span>
            )}
          </React.Fragment>
        ))}
      </div>

      {/* Quick Actions / Notifications */}
      <div className="flex items-center gap-4">
        {/* Stage Indicator Badge */}
        <Badge variant="accent" className="hidden sm:inline-flex text-[10px] font-bold tracking-wider py-0.5 px-2">
          GIAI ĐOẠN 4
        </Badge>

        {/* Cart Badge */}
        <CartBadge />

        {/* Notifications Icon (Placeholder) */}
        <button className="p-1.5 rounded-full text-stone-400 hover:text-stone-600 hover:bg-stone-50 transition-colors focus:outline-none">
          <Bell size={18} />
        </button>

        {/* User initials / Quick Profile Link */}
        {user && (
          <div className="flex items-center gap-2 border-l border-stone-200 pl-4">
            <div className="w-8 h-8 rounded-full bg-stone-100 flex items-center justify-center text-stone-600 border border-stone-200">
              <User size={16} />
            </div>
            <span className="hidden md:inline text-xs font-semibold text-stone-700">
              {user.fullName}
            </span>
          </div>
        )}
      </div>
    </header>
  );
}
