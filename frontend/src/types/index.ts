// Role codes
export type RoleCode = 'CUSTOMER' | 'STAFF' | 'MANAGER' | 'ADMIN';

// User Status
export type UserStatus = 'PENDING_VERIFY' | 'ACTIVE' | 'LOCKED' | 'INACTIVE';

// Order Type
export type OrderType = 'ONLINE' | 'POS';

// Order Status
export type OrderStatus =
  | 'CHO_XAC_NHAN'
  | 'DA_XAC_NHAN'
  | 'DANG_CHE_BIEN'
  | 'DANG_GIAO'
  | 'DANG_PHUC_VU'
  | 'HOAN_THANH'
  | 'DA_HUY';

// Kitchen Item Status
export type KitchenItemStatus = 'CHO' | 'DANG_NAU' | 'DA_XONG';

// Payment Status
export type PaymentStatus = 'PENDING' | 'SUCCESS' | 'FAILED' | 'CANCELED' | 'EXPIRED' | 'REFUNDED';

// Payment Method
export type PaymentMethod = 'CASH' | 'COD' | 'BANK_TRANSFER' | 'ONLINE_GATEWAY';

// Review Moderation Status
export type ReviewModerationStatus = 'PENDING' | 'APPROVED' | 'REJECTED';

// Table Status
export type TableStatus = 'AVAILABLE' | 'OCCUPIED' | 'CLEANING' | 'INACTIVE';

// API Response Format
export interface ApiResponse<T = any> {
  success: boolean;
  message: string;
  data?: T;
  meta?: any;
  timestamp: string;
}

// API Error Response Format
export interface ApiErrorResponse {
  success: boolean;
  message: string;
  errorCode?: string;
  errors?: any[];
  timestamp: string;
}

// User Context
export interface UserPayload {
  userId: string;
  email: string;
  roles: RoleCode[];
}

// Category Entity
export interface Category {
  id: string;
  categoryName: string;
  description?: string;
  displayOrder: number;
  isActive: boolean;
  createdAt?: string;
  updatedAt?: string;
}

// Category Summary for Products
export interface CategorySummary {
  id: string;
  categoryName: string;
}

// Product Entity
export interface Product {
  id: string;
  productName: string;
  slug: string;
  basePrice: number;
  description?: string;
  imageUrl?: string;
  isAvailable: boolean;
  isFeatured: boolean;
  preparationTimeMinutes: number;
  category: CategorySummary;
  createdAt?: string;
  updatedAt?: string;
}

// Option Entity
export interface Option {
  id: string;
  optionName: string;
  incrementalPrice: number;
  isAvailable: boolean;
  displayOrder: number;
  createdAt?: string;
  updatedAt?: string;
}

// Option Group Entity
export interface OptionGroup {
  id: string;
  groupName: string;
  isRequired: boolean;
  minSelectable: number;
  maxSelectable: number;
  displayOrder: number;
  isActive: boolean;
  createdAt?: string;
  updatedAt?: string;
  options: Option[];
}

// Page Metadata format
export interface PageMetadata {
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

// ============ Cart Types ============

export interface CartOptionResponse {
  optionId: string;
  optionName: string;
  incrementalPrice: number;
}

export interface CartItemResponse {
  id: string;
  productId: string;
  productName: string;
  imageUrl?: string;
  basePrice: number;
  quantity: number;
  options: CartOptionResponse[];
  specialNote?: string;
  unitPriceSnapshot: number;
  currentUnitPrice: number;
  priceChanged: boolean;
  lineTotal: number;
  isValid: boolean;
  validationErrors: string[];
}

export interface VoucherSummary {
  code: string;
  discountType: 'PERCENTAGE' | 'FIXED_AMOUNT';
  discountValue: number;
}

export interface CartResponse {
  cartId: string;
  items: CartItemResponse[];
  subtotal: number;
  discountAmount: number;
  estimatedTotal: number;
  itemCount: number;
  isCartValid: boolean;
  appliedVoucher?: VoucherSummary;
  voucherRemovalReason?: string;
}

export interface CartMergeAccepted {
  clientItemId: string;
  cartItemId: string;
  merged: boolean;
  newQuantity: number;
}

export interface CartMergeRejected {
  clientItemId: string;
  reason: string;
}

export interface CartMergeResponse {
  acceptedItems: CartMergeAccepted[];
  rejectedItems: CartMergeRejected[];
  cart: CartResponse;
}

// Guest Cart (localStorage)
export interface GuestCartItem {
  clientItemId: string;
  productId: string;
  productName: string;
  imageUrl?: string;
  optionIds: string[];
  optionNames: string[];
  quantity: number;
  specialNote?: string;
  displayPrice: number; // for display only, NOT trusted by backend
}

// ============ Address Types ============
export interface AddressRequest {
  receiverName: string;
  receiverPhone: string;
  addressDetail: string;
  addressLabel?: string;
  isDefault?: boolean;
}

export interface AddressResponse {
  id: string;
  receiverName: string;
  receiverPhone: string;
  addressDetail: string;
  addressLabel?: string;
  isDefault: boolean;
  createdAt: string;
  updatedAt: string;
}

// ============ Checkout & Order Types ============
export interface CheckoutPreviewRequest {
  addressId?: string;
  paymentMethod?: string;
  customerNote?: string;
}

export interface CheckoutPreviewResponse {
  address?: AddressResponse;
  items: CartItemResponse[];
  subtotal: number;
  appliedVoucherCode?: string;
  discountAmount: number;
  shippingFee: number;
  finalAmount: number;
  paymentMethod?: string;
  warnings: string[];
  valid: boolean;
}

export interface OrderItemOptionResponse {
  id: string;
  optionGroupNameSnapshot: string;
  optionNameSnapshot: string;
  incrementalPriceSnapshot: number;
}

export interface OrderItemResponse {
  id: string;
  productNameSnapshot: string;
  basePriceSnapshot: number;
  optionsPriceSnapshot: number;
  unitPriceSnapshot: number;
  lineTotal: number;
  specialNote?: string;
  quantity: number;
  options: OrderItemOptionResponse[];
}

export interface OrderResponse {
  orderId: string;
  orderCode: string;
  orderType: OrderType;
  status: OrderStatus;
  paymentMethod: PaymentMethod;
  paymentStatus: PaymentStatus;
  subtotal: number;
  discountAmount: number;
  finalAmount: number;
  createdAt: string;
  receiverNameSnapshot?: string;
  receiverPhoneSnapshot?: string;
  shippingAddressSnapshot?: string;
  notes?: string;
  shippingFee?: number;
  voucherCodeSnapshot?: string;
  items?: OrderItemResponse[];
  canReview?: boolean;
  reviewId?: string | null;
  statusUpdatedAt?: string;
  tableId?: string | null;
  tableNumber?: string | null;
}

export interface OrderStatusHistoryResponse {
  previousStatus: OrderStatus | null;
  status: OrderStatus;
  changedAt: string;
  changedByRole: string;
  source: string;
  reason?: string;
}

export interface OrderTrackingResponse {
  orderId: string;
  orderCode: string;
  currentStatus: OrderStatus;
  statusUpdatedAt: string;
  terminal: boolean;
  timeline: OrderStatusHistoryResponse[];
}

export interface OrderSummaryResponse {
  orderId: string;
  orderCode: string;
  createdAt: string;
  status: OrderStatus;
  paymentMethod: PaymentMethod;
  paymentStatus: PaymentStatus;
  finalAmount: number;
  totalQuantity: number;
  itemPreview: string;
  canReview: boolean;
  reviewStatus: 'NONE' | 'PENDING' | 'APPROVED' | 'REJECTED';
}

export interface ReviewRequest {
  rating: number;
  comment?: string;
}

export interface ReviewResponse {
  id: string;
  orderId: string;
  rating: number;
  comment?: string;
  moderationStatus: ReviewModerationStatus;
  createdAt: string;
}

export interface PaymentResponse {
  orderId: string;
  paymentId: string;
  provider: string;
  paymentUrl?: string;
  paymentStatus: PaymentStatus;
  amount: number;
  providerTransactionId?: string;
  paidAt?: string;
  createdAt?: string;
}
export type RestaurantTableStatus = 'AVAILABLE' | 'OCCUPIED' | 'CLEANING' | 'INACTIVE';

export interface RestaurantTable {
  id: string;
  tableNumber: string;
  capacity: number;
  status: RestaurantTableStatus;
  statusUpdatedAt?: string;
  version: number;
}
