// Shared types for the order-tracking MFE. These mirror the contracts exposed
// by order-service (/api/orders) and delivery-service (/api/deliveries) through
// the API gateway — see PLATFORM_SPEC §3.

export type OrderStatus =
  | 'CREATED'
  | 'CONFIRMED'
  | 'PREPARING'
  | 'READY'
  | 'PICKED_UP'
  | 'DELIVERED'
  | 'CANCELLED';

export type DeliveryStatus =
  | 'PENDING'
  | 'ASSIGNED'
  | 'EN_ROUTE_TO_PICKUP'
  | 'PICKED_UP'
  | 'EN_ROUTE_TO_CUSTOMER'
  | 'DELIVERED'
  | 'FAILED';

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

export interface OrderItem {
  menuItemId: string;
  name: string;
  qty: number;
  unitPriceCents: number;
}

export interface Order {
  id: string;
  userId: string;
  restaurantId: string;
  status: OrderStatus;
  items: OrderItem[];
  pricing: PricingSnapshot;
  createdAt: string;
}

export interface TrackingPoint {
  lat: number;
  lng: number;
  recordedAt: string;
}

export interface Delivery {
  id: string;
  orderId: string;
  driverId: string | null;
  status: DeliveryStatus;
  trackingPoints: TrackingPoint[];
}

export interface Tracking {
  deliveryId: string;
  status: DeliveryStatus;
  points: TrackingPoint[];
  updatedAt: string;
}
