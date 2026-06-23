// Shared types for the QuickBite driver portal MFE.
// Shapes mirror driver-service and delivery-service contracts (PLATFORM_SPEC §3)
// as exposed through the gateway /api/drivers/** and /api/deliveries/** routes.

export interface Geo {
  lat: number;
  lng: number;
}

// --- driver-service ---

export type DriverStatus = 'OFFLINE' | 'AVAILABLE' | 'ON_DELIVERY';

export interface Driver {
  id: string;
  userId: string;
  name: string;
  vehicle: string;
  status: DriverStatus;
  geo: Geo | null;
}

// PATCH /api/drivers/{id}/availability
export interface SetAvailabilityRequest {
  // The driver-service maps "available" -> AVAILABLE / OFFLINE; a driver that is
  // ON_DELIVERY cannot be toggled offline (the service rejects it).
  available: boolean;
}

// POST /api/drivers/{id}/location
export interface LocationPing {
  lat: number;
  lng: number;
  // ISO-8601 client timestamp; the service stamps its own authoritative time.
  recordedAt?: string;
}

// --- delivery-service ---

export type DeliveryStatus =
  | 'PENDING'
  | 'ASSIGNED'
  | 'EN_ROUTE_TO_PICKUP'
  | 'PICKED_UP'
  | 'EN_ROUTE_TO_CUSTOMER'
  | 'DELIVERED'
  | 'FAILED';

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
  pickupAddress: string;
  dropoffAddress: string;
  customerName: string;
  payoutCents: number;
  trackingPoints: TrackingPoint[];
  createdAt: string;
  updatedAt: string;
}

// PATCH /api/deliveries/{id}/status
export interface AdvanceDeliveryRequest {
  status: DeliveryStatus;
}

// Earnings are derived client-side from the courier's DELIVERED deliveries.
export interface EarningsSummary {
  completedCount: number;
  activeCount: number;
  totalCents: number;
  currency: string;
}
