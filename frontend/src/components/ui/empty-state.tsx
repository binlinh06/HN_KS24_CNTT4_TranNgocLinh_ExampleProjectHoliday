import React from 'react';
import { Inbox } from 'lucide-react';

export interface EmptyStateProps {
  title: string;
  description: string;
}

export const EmptyState: React.FC<EmptyStateProps> = ({ title, description }) => {
  return (
    <div className="flex flex-col items-center justify-center p-12 text-center bg-stone-50 border border-dashed border-stone-200 rounded-xl">
      <div className="p-4 bg-stone-100 rounded-full text-stone-400 mb-4">
        <Inbox size={32} />
      </div>
      <h3 className="text-base font-semibold text-stone-900 mb-1">{title}</h3>
      <p className="text-sm text-stone-500 max-w-sm">{description}</p>
    </div>
  );
};
