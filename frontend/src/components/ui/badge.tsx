import React from 'react';
import clsx from 'clsx';

type BadgeVariant = 'primary' | 'accent' | 'success' | 'error' | 'warning' | 'info' | 'neutral';

interface BadgeProps {
  children: React.ReactNode;
  variant?: BadgeVariant;
  className?: string;
}

const variantStyles: Record<BadgeVariant, string> = {
  primary: 'bg-primary-50 text-primary-700 border-primary-200',
  accent: 'bg-accent-50 text-accent-700 border-accent-200',
  success: 'bg-success-50 text-green-700 border-green-200',
  error: 'bg-error-50 text-red-700 border-red-200',
  warning: 'bg-warning-50 text-amber-700 border-amber-200',
  info: 'bg-info-50 text-blue-700 border-blue-200',
  neutral: 'bg-stone-100 text-stone-600 border-stone-200',
};

export function Badge({ children, variant = 'neutral', className }: BadgeProps) {
  return (
    <span
      className={clsx(
        'inline-flex items-center px-2.5 py-0.5 rounded-md text-xs font-semibold border',
        variantStyles[variant],
        className
      )}
    >
      {children}
    </span>
  );
}
