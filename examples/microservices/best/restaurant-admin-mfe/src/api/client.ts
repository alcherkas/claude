import axios, { type AxiosInstance } from 'axios';
import type {
  AdvanceOrderRequest,
  CreateMenuItemRequest,
  CreatePromotionRequest,
  CreateRestaurantRequest,
  MenuItem,
  Order,
  OrderQuery,
  Promotion,
  Restaurant,
  RestaurantStatus,
  UpdateAvailabilityRequest,
  UpdateRestaurantStatusRequest,
} from '../types';

// Kept in sync with the shell host's AuthProvider (PLATFORM_SPEC §2.3): the host
// persists the JWT under this key and every MFE reads it from localStorage.
export const TOKEN_STORAGE_KEY = 'quickbite.token';

// All external traffic enters at the gateway (PLATFORM_SPEC §2.6).
export const GATEWAY_URL: string =
  import.meta.env.VITE_GATEWAY_URL || 'http://localhost:8080';

export const apiClient: AxiosInstance = axios.create({
  baseURL: GATEWAY_URL,
  headers: { 'Content-Type': 'application/json' },
});

// Attach the persisted bearer token to every outgoing request.
apiClient.interceptors.request.use((config) => {
  const token = localStorage.getItem(TOKEN_STORAGE_KEY);
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// On a 401 the session is stale — clear it so the host drops to logged-out.
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error?.response?.status === 401) {
      localStorage.removeItem(TOKEN_STORAGE_KEY);
    }
    return Promise.reject(error);
  },
);

// --- restaurant-service (gateway route /api/restaurants/**) ---

// GET /api/restaurants — scoped server-side to the authenticated owner.
export async function myRestaurants(): Promise<Restaurant[]> {
  const { data } = await apiClient.get<Restaurant[]>('/api/restaurants');
  return data;
}

// POST /api/restaurants
export async function createRestaurant(
  body: CreateRestaurantRequest,
): Promise<Restaurant> {
  const { data } = await apiClient.post<Restaurant>('/api/restaurants', body);
  return data;
}

// PATCH /api/restaurants/{id}/status
export async function setStatus(
  id: string,
  status: RestaurantStatus,
): Promise<Restaurant> {
  const body: UpdateRestaurantStatusRequest = { status };
  const { data } = await apiClient.patch<Restaurant>(
    `/api/restaurants/${id}/status`,
    body,
  );
  return data;
}

// --- menu-service (gateway route /api/menu/**) ---

// GET /api/menu?restaurantId=
export async function listMenu(restaurantId: string): Promise<MenuItem[]> {
  const { data } = await apiClient.get<MenuItem[]>('/api/menu', {
    params: { restaurantId },
  });
  return data;
}

// POST /api/menu
export async function createMenuItem(
  body: CreateMenuItemRequest,
): Promise<MenuItem> {
  const { data } = await apiClient.post<MenuItem>('/api/menu', body);
  return data;
}

// PATCH /api/menu/{id}/availability
export async function toggleAvailability(
  id: string,
  available: boolean,
): Promise<MenuItem> {
  const body: UpdateAvailabilityRequest = { available };
  const { data } = await apiClient.patch<MenuItem>(
    `/api/menu/${id}/availability`,
    body,
  );
  return data;
}

// --- order-service (gateway route /api/orders/**) ---

// GET /api/orders?restaurantId=&status= — incoming orders for the owner.
export async function restaurantOrders(query: OrderQuery): Promise<Order[]> {
  const params: Record<string, string> = { restaurantId: query.restaurantId };
  if (query.status) {
    params.status = query.status;
  }
  const { data } = await apiClient.get<Order[]>('/api/orders', { params });
  return data;
}

// PATCH /api/orders/{id}/status
export async function advanceOrder(
  id: string,
  body: AdvanceOrderRequest,
): Promise<Order> {
  const { data } = await apiClient.patch<Order>(`/api/orders/${id}/status`, body);
  return data;
}

// --- promotion-service (gateway route /api/promotions/**) ---

// GET /api/promotions?restaurantId=
export async function promotions(restaurantId?: string): Promise<Promotion[]> {
  const { data } = await apiClient.get<Promotion[]>('/api/promotions', {
    params: restaurantId ? { restaurantId } : undefined,
  });
  return data;
}

// POST /api/promotions
export async function createPromotion(
  body: CreatePromotionRequest,
): Promise<Promotion> {
  const { data } = await apiClient.post<Promotion>('/api/promotions', body);
  return data;
}
