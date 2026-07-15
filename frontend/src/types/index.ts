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
export type PaymentStatus = 'PENDING' | 'SUCCESS' | 'FAILED' | 'REFUNDED';

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
