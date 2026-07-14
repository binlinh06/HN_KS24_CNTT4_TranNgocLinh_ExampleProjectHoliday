import React from 'react';

export interface RoleBadgeProps {
  role: string;
}

export const RoleBadge: React.FC<RoleBadgeProps> = ({ role }) => {
  const getRoleStyle = (r: string) => {
    const mapper: Record<string, string> = {
      'ADMIN': 'bg-red-600 text-white',
      'MANAGER': 'bg-amber-600 text-white',
      'STAFF': 'bg-blue-600 text-white',
      'CUSTOMER': 'bg-stone-600 text-white',
    };
    return mapper[r] || 'bg-stone-400 text-white';
  };

  const getRoleName = (r: string) => {
    const mapper: Record<string, string> = {
      'ADMIN': 'Admin',
      'MANAGER': 'Quản lý',
      'STAFF': 'Nhân viên',
      'CUSTOMER': 'Khách hàng',
    };
    return mapper[r] || r;
  };

  return (
    <span className={`inline-flex items-center px-2 py-0.5 rounded text-xs font-bold uppercase tracking-wider shadow-sm ${getRoleStyle(role)}`}>
      {getRoleName(role)}
    </span>
  );
};
