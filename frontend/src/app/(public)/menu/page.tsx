'use client';

import React, { useState, useEffect, Suspense } from 'react';
import { useRouter, useSearchParams, usePathname } from 'next/navigation';
import { Search, Clock, Check, X, SlidersHorizontal, ShoppingBag, ShieldAlert } from 'lucide-react';
import { useProducts } from '@/features/products/api';
import { useCategories } from '@/features/categories/api';
import { useProductOptions } from '@/features/product-options/api';
import { Product, Option, OptionGroup } from '@/types';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Card, CardContent } from '@/components/ui/card';
import { Modal } from '@/components/ui/modal';
import { showToast } from '@/components/ui/toast';
import { calculateItemPrice } from '@/utils/price';

function PublicMenuPageContent() {
  const router = useRouter();
  const pathname = usePathname();
  const searchParams = useSearchParams();

  // Read URL params
  const categoryIdParam = searchParams.get('category') || '';
  const keywordParam = searchParams.get('keyword') || '';
  const sortParam = searchParams.get('sort') || 'newest';
  const pageParam = parseInt(searchParams.get('page') || '0', 10);

  // States
  const [keywordInput, setKeywordInput] = useState(keywordParam);
  const [selectedProduct, setSelectedProduct] = useState<Product | null>(null);

  // Sync keyword input with URL param changes
  useEffect(() => {
    setKeywordInput(keywordParam);
  }, [keywordParam]);

  // Debounce search input
  useEffect(() => {
    const handler = setTimeout(() => {
      updateUrlParams({ keyword: keywordInput, page: 0 });
    }, 400);

    return () => clearTimeout(handler);
  }, [keywordInput]);

  // API Queries
  const { data: categories = [], isLoading: isLoadingCategories } = useCategories();
  
  const { data: paginatedData, isLoading: isLoadingProducts, isError } = useProducts({
    categoryId: categoryIdParam || undefined,
    keyword: keywordParam || undefined,
    sort: sortParam,
    isAvailable: true, // Only show available to customers
    page: pageParam,
    size: 12,
  });

  const products = paginatedData?.products || [];
  const meta = paginatedData?.meta || { page: 0, size: 12, totalElements: 0, totalPages: 0 };

  // Helper to update URL search params
  const updateUrlParams = (updates: Record<string, string | number | undefined | null>) => {
    const params = new URLSearchParams(searchParams.toString());
    Object.entries(updates).forEach(([key, val]) => {
      if (val === undefined || val === null || val === '') {
        params.delete(key);
      } else {
        params.set(key, String(val));
      }
    });
    router.push(`${pathname}?${params.toString()}`);
  };

  const handleCategorySelect = (id: string) => {
    updateUrlParams({ category: id, page: 0 });
  };

  const handleSortSelect = (sortVal: string) => {
    updateUrlParams({ sort: sortVal, page: 0 });
  };

  const handlePageSelect = (pageNum: number) => {
    updateUrlParams({ page: pageNum });
  };

  return (
    <div className="space-y-8 pb-16">
      {/* Hero Section */}
      <div className="relative overflow-hidden bg-primary text-white rounded-2xl py-12 px-6 sm:px-12 shadow-lg flex flex-col justify-center min-h-[220px]">
        <div className="absolute inset-0 bg-black/10 z-0" />
        <div className="relative z-10 max-w-2xl space-y-3">
          <Badge className="bg-accent text-primary-dark font-extrabold hover:bg-accent border-transparent">THỰC ĐƠN HÔM NAY</Badge>
          <h1 className="text-3xl sm:text-4xl font-extrabold tracking-tight">Tinh Hoa Phở Việt</h1>
          <p className="text-white/80 text-sm sm:text-base leading-relaxed">
            Hương vị nước dùng gia truyền hầm xương bò suốt 24 giờ hòa quyện cùng bánh phở mềm dẻo và thịt bò tươi ngon thượng hạng.
          </p>
        </div>
        {/* Subtle Decorative Bowl Icon in Background */}
        <div className="absolute right-8 bottom-4 text-9xl opacity-10 pointer-events-none select-none hidden md:block">
          🥣
        </div>
      </div>

      {/* Filter and Sort Toolbar */}
      <div className="space-y-4">
        <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-4">
          {/* Categories Chips */}
          <div className="flex items-center space-x-2 overflow-x-auto pb-2 scrollbar-none">
            <button
              onClick={() => handleCategorySelect('')}
              className={`px-4 py-2 rounded-full text-sm font-semibold whitespace-nowrap transition-colors border ${
                !categoryIdParam
                  ? 'bg-primary border-primary text-white shadow-sm'
                  : 'bg-white border-stone-200 text-stone-600 hover:bg-stone-50'
              }`}
            >
              Tất cả
            </button>
            {categories.map((c) => (
              <button
                key={c.id}
                onClick={() => handleCategorySelect(c.id)}
                className={`px-4 py-2 rounded-full text-sm font-semibold whitespace-nowrap transition-colors border ${
                  categoryIdParam === c.id
                    ? 'bg-primary border-primary text-white shadow-sm'
                    : 'bg-white border-stone-200 text-stone-600 hover:bg-stone-50'
                }`}
              >
                {c.categoryName}
              </button>
            ))}
          </div>

          {/* Search & Sort Panel */}
          <div className="flex items-center gap-2 self-stretch md:self-auto">
            {/* Search Input */}
            <div className="relative flex-1 md:w-64">
              <span className="absolute inset-y-0 left-0 flex items-center pl-3 text-stone-400">
                <Search size={16} />
              </span>
              <input
                type="text"
                placeholder="Tìm món ăn..."
                value={keywordInput}
                onChange={(e) => setKeywordInput(e.target.value)}
                className="w-full pl-9 pr-4 py-2 border border-stone-200 rounded-lg text-sm text-stone-900 focus:outline-none focus:ring-2 focus:ring-primary focus:border-transparent placeholder:text-stone-400"
              />
            </div>

            {/* Sort Select */}
            <div className="relative">
              <select
                value={sortParam}
                onChange={(e) => handleSortSelect(e.target.value)}
                className="pl-3 pr-8 py-2 border border-stone-200 rounded-lg text-sm text-stone-700 focus:outline-none focus:ring-2 focus:ring-primary bg-white appearance-none min-w-[130px]"
              >
                <option value="newest">Mới nhất</option>
                <option value="priceAsc">Giá tăng dần</option>
                <option value="priceDesc">Giá giảm dần</option>
                <option value="nameAsc">Tên A-Z</option>
                <option value="nameDesc">Tên Z-A</option>
              </select>
              <div className="absolute inset-y-0 right-0 flex items-center pr-2.5 pointer-events-none text-stone-400">
                <SlidersHorizontal size={14} />
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Menu Grid */}
      {isLoadingProducts ? (
        <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-6">
          {[1, 2, 3, 4, 5, 6].map((i) => (
            <div key={i} className="h-80 bg-stone-100 rounded-xl animate-pulse" />
          ))}
        </div>
      ) : isError ? (
        <Card className="border-red-100 bg-red-50/20">
          <CardContent className="flex flex-col items-center justify-center py-12 text-center">
            <div className="w-12 h-12 rounded-full bg-red-100 text-red-600 flex items-center justify-center mb-4">
              ⚠️
            </div>
            <h3 className="text-lg font-semibold text-stone-900 mb-2">Đã xảy ra lỗi</h3>
            <p className="text-stone-500 mb-4 max-w-sm">
              Không thể tải dữ liệu thực đơn từ máy chủ. Vui lòng kiểm tra kết nối.
            </p>
          </CardContent>
        </Card>
      ) : products.length === 0 ? (
        <Card>
          <CardContent className="flex flex-col items-center justify-center py-12 text-center">
            <div className="text-5xl mb-4">🥣</div>
            <h3 className="text-lg font-semibold text-stone-900 mb-2">Không tìm thấy món</h3>
            <p className="text-stone-500 max-w-sm">
              Không tìm thấy món phù hợp với tiêu chí tìm kiếm.
            </p>
          </CardContent>
        </Card>
      ) : (
        <>
          <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-6">
            {products.map((p) => (
              <Card key={p.id} className="hover:shadow-md transition-all flex flex-col group overflow-hidden border-stone-200">
                {/* Product Image */}
                <div className="relative h-48 w-full bg-stone-50 overflow-hidden flex items-center justify-center border-b border-stone-100">
                  {p.imageUrl ? (
                    <img
                      src={p.imageUrl}
                      alt={p.productName}
                      className="object-cover w-full h-full group-hover:scale-105 transition-transform duration-300"
                    />
                  ) : (
                    <div className="text-stone-300 text-6xl">🥣</div>
                  )}

                  {/* Preparation Time */}
                  <div className="absolute bottom-2 left-2 bg-white/90 backdrop-blur-sm px-2.5 py-1 rounded-full text-xs font-semibold text-stone-600 flex items-center space-x-1 shadow-sm">
                    <Clock size={12} />
                    <span>{p.preparationTimeMinutes} phút</span>
                  </div>

                  {/* Featured Badge */}
                  {p.isFeatured && (
                    <div className="absolute top-2 left-2">
                      <Badge className="bg-amber-500 text-white hover:bg-amber-500 border-none font-bold">Nổi bật</Badge>
                    </div>
                  )}

                  {/* Out of Stock Mask */}
                  {!p.isAvailable && (
                    <div className="absolute inset-0 bg-black/40 backdrop-blur-[1px] flex items-center justify-center text-white font-bold text-sm">
                      TẠM HẾT MÓN
                    </div>
                  )}
                </div>

                {/* Content */}
                <CardContent className="p-4 flex-1 flex flex-col justify-between">
                  <div className="space-y-1.5">
                    <span className="text-[10px] font-bold text-primary uppercase tracking-widest">
                      {p.category.categoryName}
                    </span>
                    <h3 className="font-bold text-stone-900 text-base line-clamp-1">{p.productName}</h3>
                    <p className="text-xs text-stone-500 line-clamp-2 min-h-[32px]">
                      {p.description || 'Hương vị thơm ngon truyền thống chuẩn quán Phở Việt.'}
                    </p>
                  </div>

                  <div className="flex items-center justify-between pt-4 border-t border-stone-50 mt-4">
                    <span className="text-lg font-extrabold text-primary">
                      {p.basePrice.toLocaleString('vi-VN')}đ
                    </span>

                    <Button
                      variant={p.isAvailable ? 'primary' : 'secondary'}
                      disabled={!p.isAvailable}
                      onClick={() => setSelectedProduct(p)}
                      className="text-xs px-4 py-1.5 h-auto w-auto flex items-center space-x-1"
                    >
                      <ShoppingBag size={14} />
                      <span>{p.isAvailable ? 'Chọn món' : 'Tạm hết'}</span>
                    </Button>
                  </div>
                </CardContent>
              </Card>
            ))}
          </div>

          {/* Pagination */}
          {meta.totalPages > 1 && (
            <div className="flex justify-center space-x-2 pt-8">
              {Array.from({ length: meta.totalPages }).map((_, idx) => (
                <Button
                  key={idx}
                  variant={pageParam === idx ? 'primary' : 'secondary'}
                  onClick={() => handlePageSelect(idx)}
                  className="px-3.5 py-1 text-sm min-w-[40px]"
                >
                  {idx + 1}
                </Button>
              ))}
            </div>
          )}
        </>
      )}

      {/* Product Customization Modal */}
      {selectedProduct && (
        <ProductCustomizationModal
          product={selectedProduct}
          onClose={() => setSelectedProduct(null)}
        />
      )}
    </div>
  );
}

export default function PublicMenuPage() {
  return (
    <Suspense fallback={
      <div className="flex items-center justify-center min-h-[400px] text-stone-500">
        Đang tải thực đơn...
      </div>
    }>
      <PublicMenuPageContent />
    </Suspense>
  );
}

// Product Customization Modal (UC-06) Helper Component
interface CustomizationProps {
  product: Product;
  onClose: () => void;
}

const ProductCustomizationModal: React.FC<CustomizationProps> = ({ product, onClose }) => {
  const { data: optionGroups = [], isLoading } = useProductOptions(product.id);

  // States
  const [selectedOptions, setSelectedOptions] = useState<Record<string, Option[]>>({});
  const [quantity, setQuantity] = useState(1);
  const [note, setNote] = useState('');
  const [validationErrors, setValidationErrors] = useState<Record<string, string>>({});

  // Initialize selected options with defaults
  useEffect(() => {
    if (optionGroups.length > 0) {
      const initial: Record<string, Option[]> = {};
      optionGroups.forEach((group) => {
        // Find default option if available (usually the first one for required group with maxSelectable=1)
        if (group.isRequired && group.minSelectable === 1 && group.maxSelectable === 1 && group.options.length > 0) {
          // Sort by display order and select first
          const sorted = [...group.options].sort((a, b) => a.displayOrder - b.displayOrder);
          initial[group.id] = [sorted[0]];
        } else {
          initial[group.id] = [];
        }
      });
      setSelectedOptions(initial);
    }
  }, [optionGroups]);

  // Price Calculation list
  const allSelectedOptionsList = Object.values(selectedOptions).flat();
  const totalPrice = calculateItemPrice(product.basePrice, allSelectedOptionsList, quantity);

  // Selection Handler
  const handleOptionSelect = (group: OptionGroup, option: Option, isSelected: boolean) => {
    setSelectedOptions((prev) => {
      const currentGroupSelections = prev[group.id] || [];
      let updated: Option[] = [];

      if (group.maxSelectable === 1) {
        // Radio behavior
        updated = isSelected ? [option] : [];
      } else {
        // Checkbox behavior
        if (isSelected) {
          if (currentGroupSelections.length < group.maxSelectable) {
            updated = [...currentGroupSelections, option];
          } else {
            // Reached limit, ignore selection or replace
            showToast.error(`Tối đa chỉ chọn ${group.maxSelectable} tùy chọn cho nhóm "${group.groupName}"`);
            return prev;
          }
        } else {
          updated = currentGroupSelections.filter((o) => o.id !== option.id);
        }
      }

      // Clear validation error for this group if selected is within range
      if (updated.length >= group.minSelectable) {
        setValidationErrors((prevErr) => {
          const c = { ...prevErr };
          delete c[group.id];
          return c;
        });
      }

      return {
        ...prev,
        [group.id]: updated,
      };
    });
  };

  const handleQuantityChange = (change: number) => {
    setQuantity((prev) => Math.max(1, prev + change));
  };

  const handleAddToCart = () => {
    // Validate required options selection
    const errors: Record<string, string> = {};
    let isValid = true;

    optionGroups.forEach((group) => {
      const selections = selectedOptions[group.id] || [];
      if (selections.length < group.minSelectable) {
        errors[group.id] = `Vui lòng chọn từ ${group.minSelectable} đến ${group.maxSelectable} tùy chọn`;
        isValid = false;
      }
    });

    if (!isValid) {
      setValidationErrors(errors);
      showToast.error('Vui lòng chọn đầy đủ các tùy chọn bắt buộc trước khi thêm món.');
      return;
    }

    // Success Simulation
    showToast.success('Cấu hình món hợp lệ. Giỏ hàng sẽ được hoàn thiện trong Giai đoạn 4.');
    onClose();
  };

  return (
    <Modal isOpen={true} onClose={onClose} title="Tùy chỉnh món ăn">
      <div className="space-y-6">
        {/* Product Details Header */}
        <div className="flex items-start gap-4">
          {product.imageUrl ? (
            <img
              src={product.imageUrl}
              alt={product.productName}
              className="w-20 h-20 object-cover rounded-lg border border-stone-100"
            />
          ) : (
            <div className="w-20 h-20 bg-stone-50 rounded-lg flex items-center justify-center text-4xl border border-stone-100">
              🥣
            </div>
          )}
          <div className="space-y-1">
            <h3 className="font-bold text-stone-900 text-lg leading-snug">{product.productName}</h3>
            <p className="text-xs text-stone-500 line-clamp-2">{product.description}</p>
            <span className="text-sm font-extrabold text-primary inline-block pt-1">
              Giá cơ bản: {product.basePrice.toLocaleString('vi-VN')}đ
            </span>
          </div>
        </div>

        {/* Option Groups Selector */}
        {isLoading ? (
          <div className="py-8 text-center text-stone-500 text-sm">Đang tải tùy chọn...</div>
        ) : optionGroups.length === 0 ? (
          <div className="py-2 text-center text-stone-500 text-xs bg-stone-50 rounded-lg">
            Món ăn này không có nhóm tùy chỉnh thêm.
          </div>
        ) : (
          <div className="space-y-5 divide-y divide-stone-100 max-h-[350px] overflow-y-auto pr-1">
            {optionGroups.map((group) => {
              const selections = selectedOptions[group.id] || [];
              const hasError = !!validationErrors[group.id];

              return (
                <div key={group.id} className="pt-4 first:pt-0 space-y-3">
                  <div className="flex items-center justify-between">
                    <div>
                      <h4 className="font-bold text-stone-900 text-sm flex items-center gap-1.5">
                        {group.groupName}
                        {group.isRequired && (
                          <span className="text-[10px] font-bold bg-red-50 text-red-600 border border-red-100 px-1.5 py-0.5 rounded">
                            Bắt buộc (Tối thiểu {group.minSelectable})
                          </span>
                        )}
                      </h4>
                      <p className="text-[11px] text-stone-500">
                        {group.maxSelectable === 1 ? 'Chọn 1 loại' : `Chọn tối đa ${group.maxSelectable} loại`}
                      </p>
                    </div>
                  </div>

                  {/* Warning Alerts */}
                  {hasError && (
                    <div className="flex items-center space-x-1.5 text-xs text-red-600 bg-red-50 px-2 py-1 rounded">
                      <ShieldAlert size={14} />
                      <span>{validationErrors[group.id]}</span>
                    </div>
                  )}

                  {/* Options List inputs */}
                  <div className="grid grid-cols-1 sm:grid-cols-2 gap-2">
                    {group.options.map((option) => {
                      const isChecked = selections.some((o) => o.id === option.id);
                      const isRadio = group.maxSelectable === 1;

                      return (
                        <label
                          key={option.id}
                          className={`flex items-center justify-between p-2.5 rounded-lg border text-sm cursor-pointer select-none transition-all ${
                            isChecked
                              ? 'border-primary bg-primary/5 text-primary font-semibold'
                              : 'border-stone-200 text-stone-700 hover:bg-stone-50/60'
                          }`}
                        >
                          <div className="flex items-center space-x-2">
                            <input
                              type={isRadio ? 'radio' : 'checkbox'}
                              name={group.id}
                              checked={isChecked}
                              onChange={(e) => handleOptionSelect(group, option, e.target.checked)}
                              className="text-primary focus:ring-primary h-4 w-4 border-stone-300"
                            />
                            <span>{option.optionName}</span>
                          </div>
                          {option.incrementalPrice > 0 && (
                            <span className="text-xs text-stone-500 font-bold">
                              +{option.incrementalPrice.toLocaleString('vi-VN')}đ
                            </span>
                          )}
                        </label>
                      );
                    })}
                  </div>
                </div>
              );
            })}
          </div>
        )}

        {/* Special Instructions Notes */}
        <div className="space-y-1.5 border-t border-stone-100 pt-4">
          <label className="text-sm font-semibold text-stone-800">Ghi chú đặc biệt</label>
          <textarea
            placeholder="Ví dụ: Không lấy giá đỗ, để nước dùng riêng..."
            value={note}
            onChange={(e) => setNote(e.target.value)}
            className="w-full text-sm text-stone-900 border border-stone-200 rounded-lg p-2.5 focus:outline-none focus:ring-2 focus:ring-primary focus:border-transparent min-h-[60px] placeholder:text-stone-400"
          />
        </div>

        {/* Quantity selector and checkout actions */}
        <div className="flex items-center justify-between pt-4 border-t border-stone-100">
          <div className="flex items-center space-x-3 bg-stone-100 px-3 py-1.5 rounded-lg">
            <button
              onClick={() => handleQuantityChange(-1)}
              className="text-stone-500 hover:text-stone-900 font-bold text-lg px-2"
            >
              -
            </button>
            <span className="font-extrabold text-stone-800 w-6 text-center">{quantity}</span>
            <button
              onClick={() => handleQuantityChange(1)}
              className="text-stone-500 hover:text-stone-900 font-bold text-lg px-2"
            >
              +
            </button>
          </div>

          <div className="text-right">
            <p className="text-xs text-stone-500 font-medium">Tổng số tiền</p>
            <p className="text-xl font-extrabold text-primary">
              {totalPrice.toLocaleString('vi-VN')}đ
            </p>
          </div>
        </div>

        <div className="flex items-center justify-end space-x-3 pt-4 border-t border-stone-100">
          <Button variant="secondary" onClick={onClose}>
            Đóng
          </Button>
          <Button onClick={handleAddToCart}>
            Thêm vào giỏ hàng
          </Button>
        </div>
      </div>
    </Modal>
  );
};
