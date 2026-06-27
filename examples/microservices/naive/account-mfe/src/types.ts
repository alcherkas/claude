// Shared types for the QuickBite account MFE.
// Mirrors the contracts of identity-service, wallet-service, order-service and
// review-service (PLATFORM_SPEC §3) as consumed through the gateway.

export type UserRole = 'CUSTOMER' | 'RESTAURANT_OWNER' | 'COURIER' | 'ADMIN';

// identity-service — GET /api/users/me
export interface User {
  id: string;
  email: string;
  fullName: string;
  role: UserRole;
  createdAt: string;
}

// wallet-service — Wallet{userId,balanceCents,currency}
export interface Wallet {
  userId: string;
  balanceCents: number;
  currency: string;
}

export type WalletTxnType = 'CREDIT' | 'DEBIT';

// wallet-service — WalletTxn
export interface WalletTxn {
  id: string;
  userId: string;
  type: WalletTxnType;
  amountCents: number;
  currency: string;
  description: string;
  createdAt: string;
}

// wallet-service — POST /api/wallets/{userId}/credits request body.
export interface AddCreditRequest {
  amountCents: number;
  currency: string;
  description?: string;
}

export type OrderStatus =
  | 'CREATED'
  | 'CONFIRMED'
  | 'PREPARING'
  | 'READY'
  | 'PICKED_UP'
  | 'DELIVERED'
  | 'CANCELLED';

export interface OrderItem {
  menuItemId: string;
  name: string;
  qty: number;
  unitPriceCents: number;
}

// order-service — immutable pricing snapshot captured at order creation.
export interface PricingSnapshot {
  subtotalCents: number;
  deliveryFeeCents: number;
  serviceFeeCents: number;
  taxCents: number;
  discountCents: number;
  tipCents: number;
  totalCents: number;
  currency: string;
}

// order-service — GET /api/orders?userId=
export interface Order {
  id: string;
  userId: string;
  restaurantId: string;
  status: OrderStatus;
  items: OrderItem[];
  pricing: PricingSnapshot;
  createdAt: string;
}

// review-service — GET /api/reviews?userId=
export interface Review {
  id: string;
  orderId: string;
  userId: string;
  restaurantId: string;
  driverId?: string;
  rating: number;
  comment: string;
  createdAt: string;
}

// review-service — POST /api/reviews request body.
export interface SubmitReviewRequest {
  orderId: string;
  restaurantId: string;
  driverId?: string;
  rating: number;
  comment: string;
}
