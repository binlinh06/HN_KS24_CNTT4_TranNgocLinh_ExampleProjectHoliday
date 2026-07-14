import React from 'react';
import clsx from 'clsx';

export interface InputProps extends React.InputHTMLAttributes<HTMLInputElement> {
  label?: string;
  error?: string;
  icon?: React.ReactNode;
}

export const Input = React.forwardRef<HTMLInputElement, InputProps>(
  ({ label, error, icon, className = '', type = 'text', ...props }, ref) => {
    return (
      <div className="w-full">
        {label && (
          <label className="block text-sm font-medium text-stone-700 mb-1.5">
            {label}
          </label>
        )}
        <div className="relative">
          {icon && (
            <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-stone-400">
              {icon}
            </div>
          )}
          <input
            type={type}
            ref={ref}
            className={clsx(
              'w-full px-3 py-2.5 border rounded-lg text-sm transition-all duration-200 bg-white text-stone-900 placeholder:text-stone-400',
              'focus:outline-none focus:ring-2 focus:ring-primary-500/30 focus:border-primary-500',
              error ? 'border-error-500 focus:ring-error-500/20 focus:border-error-500' : 'border-stone-300',
              icon ? 'pl-10' : '',
              className
            )}
            {...props}
          />
        </div>
        {error && <p className="mt-1.5 text-xs text-error-500 font-medium">{error}</p>}
      </div>
    );
  }
);
Input.displayName = 'Input';
