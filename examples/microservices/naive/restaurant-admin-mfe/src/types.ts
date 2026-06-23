// Shared types for the QuickBite restaurant-admin MFE.
// Shapes mirror restaurant-service, menu-service, order-service and
// promotion-service contracts (PLATFORM_SPEC §3) as exposed through the
// gateway /api/* routes.

export type RestaurantStatus = 'PENDING' | 'ACTIVE' | 'SUSPENDED';

export interface Geo {
  lat: number;
  lng: number;
}

// --- restaurant-service (gateway route /api/restaurants/**) ---

export interface Restaurant {
  id: string;
  ownerUserId: string;
  name: string;
  cuisine: string;
  address: string;
  geo: Geo;
  status: RestaurantStatus;
  openingHours: string;
}

// POST /api/restaurants — owner creates a restaurant. ownerUserId is derived
// server-side from the JWT (X-User-Id), so it is not part of the payload.
export interface CreateRestaurantRequest {
  name: string;
  cuisine: string;
  address: string;
  geo: Geo;
  openingHours: string;
}

// PATCH /api/restaurants/{id}/status
export interface UpdateRestaurantStatusRequest {
  status: RestaurantStatus;
}

// --- menu-service (gateway route /api/menu/**) ---

export interface MenuItem {
  id: string;
  restaurantId: string;
  name: string;
  description: string;
  priceCents: number;
  currency: string;
  category: string;
  available: boolean;
}

// POST /api/menu — owner adds a menu item.
export interface CreateMenuItemRequest {
  restaurantId: string;
  name: string;
  description: string;
  priceCents: number;
  currency: string;
  category: string;
  available: boolean;
}

// PATCH /api/menu/{id}/availability
export interface UpdateAvailabilityRequest {
  available: boolean;
}

// --- order-service (gateway route /api/orders/**) ---

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

export interface OrderPricing {
  subtotalCents: number;
  deliveryFeeCents: number;
  serviceFeeCents: number;
  taxCents: number;
  discountCents: number;
  totalCents: number;
}

export interface Order {
  id: string;
  userId: string;
  restaurantId: string;
  status: OrderStatus;
  items: OrderItem[];
  pricing: OrderPricing;
  createdAt: string;
}

// Filters accepted by GET /api/orders for the owner dashboard.
export interface OrderQuery {
  restaurantId: string;
  status?: OrderStatus;
}

// PATCH /api/orders/{id}/status — advance an incoming order.
export interface AdvanceOrderRequest {
  status: OrderStatus;
}

// --- promotion-service (gateway route /api/promotions/**) ---

export type PromotionType = 'PERCENT' | 'FIXED' | 'FREE_DELIVERY';

export interface Promotion {
  code: string;
  type: PromotionType;
  // For PERCENT this is a whole-number percentage; for FIXED it is cents;
  // for FREE_DELIVERY it is ignored.
  value: number;
  minSubtotalCents: number;
  validFrom: string;
  validTo: string;
  maxRedemptions: number;
  perUserLimit: number;
  active: boolean;
  // Optional scope so an owner's promotions target their own restaurant.
  restaurantId?: string;
}

// POST /api/promotions — owner creates a promotion.
export interface CreatePromotionRequest {
  code: string;
  type: PromotionType;
  value: number;
  minSubtotalCents: number;
  validFrom: string;
  validTo: string;
  maxRedemptions: number;
  perUserLimit: number;
  active: boolean;
  restaurantId: string;
}
