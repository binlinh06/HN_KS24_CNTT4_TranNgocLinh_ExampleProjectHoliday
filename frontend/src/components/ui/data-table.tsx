import React from 'react';
import { EmptyState } from './empty-state';

export interface Column<T> {
  header: string;
  accessor: (row: T) => React.ReactNode;
  className?: string;
}

export interface DataTableProps<T> {
  columns: Column<T>[];
  data: T[];
  keyExtractor: (row: T) => string | number;
}

export function DataTable<T>({ columns, data, keyExtractor }: DataTableProps<T>) {
  if (data.length === 0) {
    return <EmptyState title="Không có dữ liệu" description="Danh sách hiện tại đang trống." />;
  }

  return (
    <div className="w-full overflow-x-auto border border-stone-200 rounded-lg">
      <table className="w-full text-sm text-left text-stone-600 bg-white">
        <thead className="text-xs uppercase bg-stone-50 border-b border-stone-200 text-stone-700 font-semibold">
          <tr>
            {columns.map((col, index) => (
              <th key={index} className={`px-6 py-4 ${col.className || ''}`}>
                {col.header}
              </th>
            ))}
          </tr>
        </thead>
        <tbody className="divide-y divide-stone-100">
          {data.map((row) => (
            <tr key={keyExtractor(row)} className="hover:bg-stone-50/50 transition-colors">
              {columns.map((col, index) => (
                <td key={index} className={`px-6 py-4 font-medium text-stone-900 ${col.className || ''}`}>
                  {col.accessor(row)}
                </td>
              ))}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
