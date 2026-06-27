// Shared types for the checkout-mfe. These mirror the contracts exposed through
// the API gateway (PLATFORM_SPEC §3/§4) for cart, pricing, promotion, order,
// payment and wallet.

// --- cart-service (/api/carts/**) -------------------------------------------

export interface CartItem {
  menuItemId: string;
  name: string;
  qty: number;
  unitPriceCents: number;
}

export interface Cart {
  userId: string;
  restaurantId: string;
  items: CartItem[];
  updatedAt: string;
}

export interface UpdateCartItemRequest {
  qty: number;
}

// --- promotion-service (/api/promotions/**) ---------------------------------

export type PromotionType = 'PERCENT' | 'FIXED' | 'FREE_DELIVERY';

export interface PromoValidation {
  code: string;
  valid: boolean;
  type?: PromotionType;
  value?: number;
  minSubtotalCents?: number;
  message?: string;
}

// --- pricing-service (/api/pricing/**) --------------------------------------

export interface QuoteLineItem {
  menuItemId: string;
  name: string;
  qty: number;
  unitPriceCents: number;
  lineTotalCents: number;
}

export interface QuoteRequest {
  userId: string;
  restaurantId: string;
  items: Array<{ menuItemId: string; qty: number }>;
  promoCode?: string;
  // Optional courier tip in cents; pricing adds it to totalCents after tax.
  tipCents?: number;
}

export interface Quote {
  subtotalCents: number;
  deliveryFeeCents: number;
  serviceFeeCents: number;
  taxCents: number;
  discountCents: number;
  tipCents: number;
  totalCents: number;
  lineItems: QuoteLineItem[];
}

// --- order-service (/api/orders/**) -----------------------------------------

export type OrderStatus =
  | 'CREATED'
  | 'CONFIRMED'
  | 'PREPARING'
  | 'READY'
  | 'PICKED_UP'
  | 'DELIVERED'
  | 'CANCELLED';

export interface PlaceOrderRequest {
  userId: string;
  restaurantId: string;
  promoCode?: string;
  // Courier tip in cents; forwarded to pricing so the persisted order total matches the quote.
  tipCents?: number;
}

export interface Order {
  id: string;
  userId: string;
  restaurantId: string;
  status: OrderStatus;
  items: CartItem[];
  pricing: Quote;
  createdAt: string;
}

// --- payment-service (/api/payments/**) -------------------------------------

export type PaymentMethod = 'CARD' | 'WALLET';

export type PaymentStatus =
  | 'AUTHORIZED'
  | 'CAPTURED'
  | 'REFUNDED'
  | 'FAILED';

export interface CardDetails {
  // Only the last4 / brand are ever persisted server-side; the raw PAN is sent
  // once to the mock PSP for the "capture".
  number: string;
  expiry: string;
  cvc: string;
  holder: string;
}

export interface PaymentRequest {
  orderId: string;
  userId: string;
  amountCents: number;
  method: PaymentMethod;
  card?: CardDetails;
}

export interface Payment {
  id: string;
  orderId: string;
  userId: string;
  amountCents: number;
  tipCents: number;
  method: PaymentMethod;
  status: PaymentStatus;
  provider: string;
}

// --- wallet-service (/api/wallets/**) ---------------------------------------

export interface Wallet {
  userId: string;
  balanceCents: number;
  currency: string;
}
