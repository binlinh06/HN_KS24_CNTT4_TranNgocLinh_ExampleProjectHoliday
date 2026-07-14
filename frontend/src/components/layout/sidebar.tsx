'use client';

import React, { useState } from 'react';
import Link from 'next/link';
import { usePathname, useRouter } from 'next/navigation';
import { useAuthStore } from '@/stores/auth';
import { Button } from '@/components/ui/button';
import { Avatar } from '@/components/ui/avatar';
import { Badge } from '@/components/ui/badge';
import { showToast } from '@/components/ui/toast';
import { 
  Utensils, 
  LogOut, 
  Menu, 
  X,
  ChevronRight
} from 'lucide-react';
import clsx from 'clsx';
import { api } from '@/lib/api';

interface SidebarLink {
  title: string;
  href: string;
  icon: React.ComponentType<any>;
}

interface SidebarProps {
  links: SidebarLink[];
  roleTitle: string;
  roleBadge: 'primary' | 'accent' | 'success' | 'error' | 'warning' | 'info' | 'neutral';
}

export function Sidebar({ links, roleTitle, roleBadge }: SidebarProps) {
  const pathname = usePathname();
  const router = useRouter();
  const { user, clearAuth } = useAuthStore();
  const [isOpen, setIsOpen] = useState(false);

  const handleLogout = async () => {
    try {
      await api.post('/auth/logout');
    } catch (e) {
      // Ignored
    } finally {
      clearAuth();
      showToast.success('Đăng xuất thành công');
      router.push('/login');
    }
  };

  const toggleSidebar = () => setIsOpen(!isOpen);

  if (!user) return null;

  return (
    <>
      {/* Mobile Toggle Button */}
      <div className="lg:hidden fixed top-3 left-4 z-50">
        <button
          onClick={toggleSidebar}
          className="p-2 rounded-lg bg-white border border-stone-200 shadow-sm text-stone-700 hover:bg-stone-50 focus:outline-none"
        >
          {isOpen ? <X size={20} /> : <Menu size={20} />}
        </button>
      </div>

      {/* Mobile Sidebar Overlay */}
      {isOpen && (
        <div
          onClick={toggleSidebar}
          className="lg:hidden fixed inset-0 z-40 bg-stone-900/40 backdrop-blur-sm"
        />
      )}

      {/* Sidebar Container */}
      <aside
        className={clsx(
          'fixed inset-y-0 left-0 z-40 w-64 bg-stone-900 text-stone-300 flex flex-col border-r border-stone-800 transition-transform duration-300 lg:translate-x-0 lg:static shrink-0',
          isOpen ? 'translate-x-0' : '-translate-x-full'
        )}
      >
        {/* Logo Section */}
        <div className="h-16 px-6 border-b border-stone-850 flex items-center gap-2.5">
          <div className="w-9 h-9 rounded-lg bg-primary-500 flex items-center justify-center text-white font-bold shadow-sm shadow-primary-900/30">
            <Utensils size={18} className="text-white" />
          </div>
          <div>
            <span className="font-bold text-white text-sm tracking-tight block">PHỞ BÒ GIA TRUYỀN</span>
            <span className="text-[9px] text-accent-400 font-bold uppercase tracking-widest block -mt-0.5">
              {roleTitle}
            </span>
          </div>
        </div>

        {/* Navigation Links */}
        <nav className="flex-1 py-6 px-4 space-y-1 overflow-y-auto custom-scrollbar">
          {links.map((link) => {
            const isActive = pathname === link.href || pathname.startsWith(link.href + '/');
            const Icon = link.icon;
            return (
              <Link
                key={link.href}
                href={link.href}
                onClick={() => setIsOpen(false)}
                className={clsx(
                  'flex items-center justify-between px-3.5 py-2.5 rounded-lg text-sm font-medium transition-all duration-200 group',
                  isActive
                    ? 'bg-primary-600 text-white shadow-sm'
                    : 'text-stone-450 hover:bg-stone-800 hover:text-white'
                )}
              >
                <span className="flex items-center gap-3">
                  <Icon 
                    size={18} 
                    className={clsx(
                      'transition-colors',
                      isActive ? 'text-white' : 'text-stone-500 group-hover:text-stone-300'
                    )} 
                  />
                  {link.title}
                </span>
                <ChevronRight 
                  size={14} 
                  className={clsx(
                    'transition-all',
                    isActive ? 'opacity-100 translate-x-0' : 'opacity-0 -translate-x-1 group-hover:opacity-50 group-hover:translate-x-0'
                  )} 
                />
              </Link>
            );
          })}
        </nav>

        {/* User Quick Info & Logout */}
        <div className="p-4 border-t border-stone-850 bg-stone-950/20">
          <div className="flex items-center gap-3 mb-4">
            <Avatar name={user.fullName} size="sm" className="bg-stone-800 text-stone-300 border-stone-700" />
            <div className="min-w-0 flex-1">
              <span className="block text-xs font-semibold text-white truncate">{user.fullName}</span>
              <span className="inline-block mt-0.5">
                <Badge variant={roleBadge} className="px-1.5 py-0 text-[9px] uppercase tracking-wider font-bold">
                  {user.roles[0]}
                </Badge>
              </span>
            </div>
          </div>
          <Button
            variant="ghost"
            className="w-full text-stone-400 hover:bg-stone-800 hover:text-white justify-start py-2 px-3 border border-transparent hover:border-stone-800"
            size="sm"
            onClick={handleLogout}
          >
            <LogOut size={16} className="mr-2.5" />
            Đăng xuất
          </Button>
        </div>
      </aside>
    </>
  );
}
