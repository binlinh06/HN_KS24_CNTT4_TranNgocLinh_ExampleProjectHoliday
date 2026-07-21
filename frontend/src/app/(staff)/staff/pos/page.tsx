'use client';

import React, { useState, useEffect } from 'react';
import { PageHeader } from '@/components/ui/page-header';
import { useCategories } from '@/features/categories/api';
import { useProducts } from '@/features/products/api';
import { useStaffTables, createPosOrder, processPosPayment } from '@/features/staff/api';
import { toast } from 'react-hot-toast';
import { v4 as uuidv4 } from 'uuid';

interface CartItem {
  id: string; // unique item id (combines product + selected option ids)
  productId: string;
  productName: string;
  basePrice: number;
  quantity: number;
  selectedOptions: {
    id: string;
    optionName: string;
    incrementalPrice: number;
    groupName: string;
  }[];
  totalPrice: number;
}

export default function StaffPosPage() {
  const { data: categories = [], isLoading: catsLoading } = useCategories();
  const { data: productsData, isLoading: prodsLoading } = useProducts({ size: 100 });
  const products = productsData?.products || [];

  const { data: tables = [] } = useStaffTables();
  const activeTables = tables.filter(t => t.status === 'OCCUPIED'); // Tables with active sessions

  const [selectedCategory, setSelectedCategory] = useState<string>('');
  const [cart, setCart] = useState<CartItem[]>([]);
  const [discount, setDiscount] = useState<number>(0);
  const [tableId, setTableId] = useState<string>(''); // empty means takeaway
  const [paymentMethod, setPaymentMethod] = useState<'CASH' | 'CARD'>('CASH');

  // Option selection modal state
  const [optionModalProduct, setOptionModalProduct] = useState<any>(null);
  const [tempSelectedOptions, setTempSelectedOptions] = useState<any[]>([]);

  // Checkout flow state
  const [createdOrder, setCreatedOrder] = useState<any>(null);
  const [cashReceived, setCashReceived] = useState<string>('');
  const [idempotencyKey, setIdempotencyKey] = useState<string>('');
  const [isSubmitting, setIsSubmitting] = useState<boolean>(false);

  // Generate idempotency key on mount
  useEffect(() => {
    setIdempotencyKey(uuidv4());
  }, []);

  const handleAddProduct = (product: any) => {
    // If product has options, open option modal
    if (product.options && product.options.length > 0) {
      setOptionModalProduct(product);
      setTempSelectedOptions([]);
      return;
    }

    // Otherwise, add directly to cart
    addToCart(product, []);
  };

  const addToCart = (product: any, selectedOpts: any[]) => {
    const optionIds = selectedOpts.map(o => o.id).sort().join('-');
    const cartItemId = `${product.id}-${optionIds}`;

    const existingIndex = cart.findIndex(item => item.id === cartItemId);
    
    let unitPrice = product.basePrice;
    selectedOpts.forEach(o => {
      unitPrice += o.incrementalPrice;
    });

    if (existingIndex > -1) {
      const updated = [...cart];
      updated[existingIndex].quantity += 1;
      updated[existingIndex].totalPrice = updated[existingIndex].quantity * unitPrice;
      setCart(updated);
    } else {
      const newItem: CartItem = {
        id: cartItemId,
        productId: product.id,
        productName: product.productName,
        basePrice: product.basePrice,
        quantity: 1,
        selectedOptions: selectedOpts,
        totalPrice: unitPrice,
      };
      setCart([...cart, newItem]);
    }
    toast.success(`Đã thêm ${product.productName} vào giỏ`);
  };

  const handleConfirmOptions = () => {
    if (!optionModalProduct) return;
    addToCart(optionModalProduct, tempSelectedOptions);
    setOptionModalProduct(null);
  };

  const handleRemoveItem = (id: string) => {
    setCart(cart.filter(item => item.id !== id));
  };

  const handleQtyChange = (id: string, delta: number) => {
    const updated = cart.map(item => {
      if (item.id === id) {
        const newQty = Math.max(1, item.quantity + delta);
        let unitPrice = item.basePrice;
        item.selectedOptions.forEach(o => {
          unitPrice += o.incrementalPrice;
        });
        return {
          ...item,
          quantity: newQty,
          totalPrice: newQty * unitPrice,
        };
      }
      return item;
    });
    setCart(updated);
  };

  const getSubtotal = () => {
    return cart.reduce((sum, item) => sum + item.totalPrice, 0);
  };

  const getFinalTotal = () => {
    return Math.max(0, getSubtotal() - discount);
  };

  const handleCheckout = async () => {
    if (cart.length === 0) {
      toast.error('Giỏ hàng trống');
      return;
    }

    setIsSubmitting(true);
    try {
      const orderPayload = {
        tableId: tableId || null,
        paymentMethod: paymentMethod,
        discountAmount: discount,
        items: cart.map(item => ({
          productId: item.productId,
          quantity: item.quantity,
          optionIds: item.selectedOptions.map(o => o.id),
        })),
      };

      const orderRes = await createPosOrder(orderPayload, idempotencyKey);
      setCreatedOrder(orderRes);
      setCashReceived('');
      toast.success('Tạo đơn hàng POS thành công');
    } catch (err: any) {
      toast.error(err.response?.data?.message || 'Có lỗi xảy ra khi tạo đơn hàng');
    } finally {
      setIsSubmitting(false);
    }
  };

  const handlePayment = async () => {
    if (!createdOrder) return;
    
    const finalAmount = createdOrder.finalAmount;
    const cashVal = parseFloat(cashReceived) || 0;

    if (paymentMethod === 'CASH' && cashVal < finalAmount) {
      toast.error('Số tiền khách đưa không đủ');
      return;
    }

    setIsSubmitting(true);
    try {
      const payPayload = {
        cashReceived: paymentMethod === 'CASH' ? cashVal : finalAmount,
        terminalReference: paymentMethod === 'CARD' ? 'POS-CARD-' + Date.now() : undefined,
        paymentChannel: paymentMethod,
      };

      const paymentRes = await processPosPayment(createdOrder.orderId, payPayload, idempotencyKey);
      toast.success('Thanh toán hóa đơn thành công');

      // Fetch invoice and print K80 in popup
      try {
        // Find invoice for this order
        const invoiceId = paymentRes.orderId; // Wait, let's open details or let details load invoice ID
        // To be safe, we open printing window redirect
        // We will query by orderId inside print page itself
        window.open(`/staff/invoices/order/${paymentRes.orderId}/print`, '_blank', 'width=600,height=800');
      } catch (e) {
        console.error('Print redirect failed', e);
      }

      // Reset state
      setCart([]);
      setDiscount(0);
      setTableId('');
      setCreatedOrder(null);
      setIdempotencyKey(uuidv4()); // generate new key for next order
    } catch (err: any) {
      toast.error(err.response?.data?.message || 'Thanh toán thất bại');
    } finally {
      setIsSubmitting(false);
    }
  };

  const filteredProducts = selectedCategory
    ? products.filter(p => p.category?.id === selectedCategory)
    : products;

  return (
    <div className="space-y-6">
      <PageHeader
        title="Gọi món tại quầy (POS)"
        description="Giao diện ghi nhận đơn hàng và xử lý thanh toán trực tiếp tại quầy thu ngân."
      />

      <div className="grid grid-cols-1 xl:grid-cols-12 gap-6 items-start">
        {/* Menu grid pane (8 cols) */}
        <div className="xl:col-span-8 space-y-6">
          {/* Categories Tab */}
          <div className="flex gap-2 overflow-x-auto pb-2 border-b border-gray-150">
            <button
              onClick={() => setSelectedCategory('')}
              className={`px-4 py-2 rounded-lg text-sm font-semibold whitespace-nowrap transition-colors duration-150 ${
                selectedCategory === '' 
                  ? 'bg-emerald-600 text-white shadow-sm' 
                  : 'bg-white border border-gray-200 text-gray-700 hover:bg-gray-50'
              }`}
            >
              Tất cả món
            </button>
            {categories.map((cat: any) => (
              <button
                key={cat.id}
                onClick={() => setSelectedCategory(cat.id)}
                className={`px-4 py-2 rounded-lg text-sm font-semibold whitespace-nowrap transition-colors duration-150 ${
                  selectedCategory === cat.id 
                    ? 'bg-emerald-600 text-white shadow-sm' 
                    : 'bg-white border border-gray-200 text-gray-700 hover:bg-gray-50'
                }`}
              >
                {cat.categoryName}
              </button>
            ))}
          </div>

          {/* Products Grid */}
          {prodsLoading ? (
            <div className="flex justify-center py-12">
              <div className="animate-spin rounded-full h-10 w-10 border-t-2 border-b-2 border-emerald-600"></div>
            </div>
          ) : (
            <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 gap-4">
              {filteredProducts.map((p: any) => (
                <button
                  key={p.id}
                  onClick={() => handleAddProduct(p)}
                  disabled={!p.isAvailable}
                  className="bg-white rounded-xl border border-gray-200 shadow-sm p-4 hover:shadow-md transition-shadow text-left flex flex-col justify-between h-36 disabled:opacity-50"
                >
                  <div>
                    <h4 className="font-bold text-gray-900 line-clamp-2">{p.productName}</h4>
                    <p className="text-xs text-gray-400 mt-1">{p.category?.categoryName}</p>
                  </div>
                  <div className="flex items-center justify-between mt-4">
                    <span className="font-extrabold text-emerald-800">{p.basePrice.toLocaleString('vi-VN')}đ</span>
                    <span className="text-[10px] bg-emerald-50 text-emerald-700 px-1.5 py-0.5 rounded font-bold">Thêm +</span>
                  </div>
                </button>
              ))}
            </div>
          )}
        </div>

        {/* Cart & Checkout pane (4 cols) */}
        <div className="xl:col-span-4 bg-white rounded-xl border border-gray-200 shadow-sm p-6 space-y-6">
          <h3 className="text-lg font-bold text-gray-950 border-b border-gray-150 pb-2">Hóa đơn POS</h3>

          {/* Cart list */}
          <div className="space-y-4 max-h-[30vh] overflow-y-auto pr-1">
            {cart.map((item) => (
              <div key={item.id} className="flex justify-between items-start text-sm">
                <div className="space-y-1">
                  <div className="font-bold text-gray-950">{item.productName}</div>
                  {item.selectedOptions.length > 0 && (
                    <div className="text-xs text-gray-500 font-semibold pl-2 border-l border-gray-200">
                      {item.selectedOptions.map(o => o.optionName).join(', ')}
                    </div>
                  )}
                  <div className="flex items-center gap-2 mt-1">
                    <button 
                      onClick={() => handleQtyChange(item.id, -1)}
                      className="w-5 h-5 flex items-center justify-center border border-gray-300 rounded hover:bg-gray-50 font-bold"
                    >
                      -
                    </button>
                    <span className="font-semibold">{item.quantity}</span>
                    <button 
                      onClick={() => handleQtyChange(item.id, 1)}
                      className="w-5 h-5 flex items-center justify-center border border-gray-300 rounded hover:bg-gray-50 font-bold"
                    >
                      +
                    </button>
                  </div>
                </div>

                <div className="text-right space-y-1">
                  <div className="font-bold text-gray-900">{item.totalPrice.toLocaleString('vi-VN')}đ</div>
                  <button 
                    onClick={() => handleRemoveItem(item.id)}
                    className="text-xs text-red-500 hover:underline"
                  >
                    Xóa
                  </button>
                </div>
              </div>
            ))}
            {cart.length === 0 && (
              <p className="text-sm text-gray-400 text-center py-8">Giỏ hàng trống.</p>
            )}
          </div>

          <div className="border-t border-gray-200 pt-4 space-y-4">
            {/* Table Session / Takeaway Selector */}
            <div className="space-y-1">
              <label className="text-xs font-bold text-gray-500 uppercase">Chọn hình thức phục vụ</label>
              <select
                value={tableId}
                onChange={(e) => setTableId(e.target.value)}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-1 focus:ring-emerald-500 text-sm"
              >
                <option value="">Mang về (Takeaway)</option>
                {activeTables.map((t: any) => (
                  <option key={t.id} value={t.id}>Bàn số {t.tableNumber} (Đang mở)</option>
                ))}
              </select>
            </div>

            {/* Discount input */}
            <div className="space-y-1">
              <label className="text-xs font-bold text-gray-500 uppercase">Chiết khấu / Giảm giá (đ)</label>
              <input
                type="number"
                value={discount || ''}
                onChange={(e) => setDiscount(Math.max(0, parseInt(e.target.value) || 0))}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-1 focus:ring-emerald-500 text-sm"
                placeholder="Nhập số tiền giảm..."
              />
            </div>

            {/* Payment Method selector */}
            <div className="space-y-1">
              <label className="text-xs font-bold text-gray-500 uppercase">Hình thức thanh toán</label>
              <div className="flex gap-2">
                <button
                  type="button"
                  onClick={() => setPaymentMethod('CASH')}
                  className={`flex-1 py-2 border rounded-lg text-sm font-semibold transition-colors duration-150 ${
                    paymentMethod === 'CASH' 
                      ? 'bg-emerald-50 border-emerald-500 text-emerald-700' 
                      : 'bg-white border-gray-200 text-gray-700 hover:bg-gray-50'
                  }`}
                >
                  Tiền mặt
                </button>
                <button
                  type="button"
                  onClick={() => setPaymentMethod('CARD')}
                  className={`flex-1 py-2 border rounded-lg text-sm font-semibold transition-colors duration-150 ${
                    paymentMethod === 'CARD' 
                      ? 'bg-emerald-50 border-emerald-500 text-emerald-700' 
                      : 'bg-white border-gray-200 text-gray-700 hover:bg-gray-50'
                  }`}
                >
                  Thẻ ngân hàng
                </button>
              </div>
            </div>

            {/* Totals */}
            <div className="space-y-2 text-sm text-right">
              <div className="flex justify-between text-gray-500">
                <span>Tạm tính:</span>
                <span>{getSubtotal().toLocaleString('vi-VN')}đ</span>
              </div>
              <div className="flex justify-between text-gray-500">
                <span>Giảm giá:</span>
                <span>-{discount.toLocaleString('vi-VN')}đ</span>
              </div>
              <div className="flex justify-between font-bold text-gray-950 text-base">
                <span>Cần thanh toán:</span>
                <span>{getFinalTotal().toLocaleString('vi-VN')}đ</span>
              </div>
            </div>

            {/* Submit checkout */}
            <button
              onClick={handleCheckout}
              disabled={isSubmitting || cart.length === 0}
              className="w-full py-3 bg-emerald-600 hover:bg-emerald-700 text-white rounded-lg font-bold text-sm shadow-sm disabled:opacity-50 transition-colors"
            >
              {isSubmitting ? 'Đang xử lý...' : 'Tạo đơn hàng'}
            </button>
          </div>
        </div>
      </div>

      {/* Option Selection Modal */}
      {optionModalProduct && (
        <div className="fixed inset-0 bg-black bg-opacity-40 flex items-center justify-center p-4 z-50">
          <div className="bg-white rounded-xl border border-gray-200 shadow-lg max-w-md w-full p-6 space-y-4">
            <h3 className="text-lg font-bold text-gray-900">Tùy chọn cho {optionModalProduct.productName}</h3>
            
            <div className="space-y-4">
              {optionModalProduct.options.map((opt: any) => (
                <label key={opt.id} className="flex items-center gap-3 p-2 hover:bg-gray-50 rounded-lg cursor-pointer">
                  <input
                    type="checkbox"
                    checked={tempSelectedOptions.some(o => o.id === opt.id)}
                    onChange={(e) => {
                      if (e.target.checked) {
                        setTempSelectedOptions([...tempSelectedOptions, opt]);
                      } else {
                        setTempSelectedOptions(tempSelectedOptions.filter(o => o.id !== opt.id));
                      }
                    }}
                    className="h-4 w-4 text-emerald-600 border-gray-300 rounded focus:ring-emerald-500"
                  />
                  <div>
                    <span className="text-sm font-semibold text-gray-800">{opt.optionName}</span>
                    <span className="text-xs text-gray-500 block">
                      +{opt.incrementalPrice.toLocaleString('vi-VN')}đ
                    </span>
                  </div>
                </label>
              ))}
            </div>

            <div className="flex justify-end gap-2 pt-4 border-t border-gray-100">
              <button
                onClick={() => setOptionModalProduct(null)}
                className="px-4 py-2 border border-gray-300 rounded-lg hover:bg-gray-50 text-sm font-semibold"
              >
                Hủy
              </button>
              <button
                onClick={handleConfirmOptions}
                className="px-4 py-2 bg-emerald-600 hover:bg-emerald-700 text-white rounded-lg text-sm font-semibold shadow-sm"
              >
                Thêm vào giỏ
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Payment Cash Modal */}
      {createdOrder && (
        <div className="fixed inset-0 bg-black bg-opacity-40 flex items-center justify-center p-4 z-50">
          <div className="bg-white rounded-xl border border-gray-200 shadow-lg max-w-md w-full p-6 space-y-4">
            <h3 className="text-lg font-bold text-gray-900">Xử lý thanh toán hóa đơn</h3>
            
            <div className="space-y-3 text-sm">
              <div className="flex justify-between">
                <span className="text-gray-500">Mã đơn hàng:</span>
                <span className="font-bold text-gray-950">{createdOrder.orderCode}</span>
              </div>
              <div className="flex justify-between text-base font-bold text-gray-950">
                <span>Số tiền cần thanh toán:</span>
                <span>{createdOrder.finalAmount.toLocaleString('vi-VN')}đ</span>
              </div>

              {paymentMethod === 'CASH' ? (
                <div className="space-y-1 border-t border-gray-100 pt-3">
                  <label className="text-xs font-bold text-gray-500 uppercase">Khách đưa (đ):</label>
                  <input
                    type="number"
                    value={cashReceived}
                    onChange={(e) => setCashReceived(e.target.value)}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-1 focus:ring-emerald-500 font-bold text-base"
                    placeholder="Nhập số tiền mặt nhận..."
                  />
                  {parseFloat(cashReceived) >= createdOrder.finalAmount && (
                    <div className="text-right text-xs font-bold text-emerald-600 mt-1">
                      Tiền thối: {(parseFloat(cashReceived) - createdOrder.finalAmount).toLocaleString('vi-VN')}đ
                    </div>
                  )}
                </div>
              ) : (
                <div className="p-3 bg-blue-50 border border-blue-200 rounded-lg text-blue-800 text-xs">
                  Vui lòng quẹt thẻ khách hàng qua thiết bị POS phụ trợ và nhấn Xác nhận thanh toán khi hoàn tất.
                </div>
              )}
            </div>

            <div className="flex justify-end gap-2 pt-4 border-t border-gray-100">
              <button
                onClick={() => setCreatedOrder(null)}
                className="px-4 py-2 border border-gray-300 rounded-lg hover:bg-gray-50 text-sm font-semibold"
              >
                Hủy / Đóng
              </button>
              <button
                onClick={handlePayment}
                disabled={isSubmitting}
                className="px-4 py-2 bg-emerald-600 hover:bg-emerald-700 text-white rounded-lg text-sm font-semibold shadow-sm"
              >
                Xác nhận & In hóa đơn
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
