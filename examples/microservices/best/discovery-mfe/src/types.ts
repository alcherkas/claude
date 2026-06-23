// Shared types for the QuickBite discovery MFE.
// Shapes mirror restaurant-service, search-service and menu-service contracts
// (PLATFORM_SPEC §3) as exposed through the gateway /api/* routes.

export type RestaurantStatus = 'PENDING' | 'ACTIVE' | 'SUSPENDED';

export interface Geo {
  lat: number;
  lng: number;
}

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

// search-service denormalized read model (SearchDoc). A hit can be a restaurant
// or a menu item; `type` discriminates and `restaurantId` always points at the
// owning restaurant.
export type SearchHitType = 'RESTAURANT' | 'MENU_ITEM';

export interface SearchHit {
  type: SearchHitType;
  refId: string;
  restaurantId: string;
  name: string;
  cuisine: string;
  priceCents: number | null;
  geo: Geo | null;
  available: boolean;
}

export interface SearchResponse {
  query: string;
  total: number;
  hits: SearchHit[];
}

// Parameters accepted by GET /api/search.
export interface SearchParams {
  q: string;
  cuisine?: string;
  // `geo` is sent as "lat,lng" plus an optional radius in km.
  geo?: Geo;
  radiusKm?: number;
}

// discovery-mfe does not own the cart (PLATFORM_SPEC §1.2 limits it to search,
// restaurant and menu). When a customer taps "Add" on a menu item we emit this
// detail on a DOM CustomEvent; the shell host listens and forwards it to
// checkout-mfe, which owns cart-service (/api/carts/**).
export const ADD_TO_CART_EVENT = 'quickbite:add-to-cart';

export interface AddToCartDetail {
  restaurantId: string;
  menuItemId: string;
  qty: number;
  unitPriceCents: number;
  name: string;
}
