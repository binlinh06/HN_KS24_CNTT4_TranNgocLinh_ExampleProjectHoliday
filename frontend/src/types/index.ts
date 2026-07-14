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
