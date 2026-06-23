import axios, { type AxiosInstance } from 'axios';
import type {
  MenuItem,
  Restaurant,
  SearchParams,
  SearchResponse,
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

// --- search-service (gateway route /api/search/**) ---

// GET /api/search?q=&cuisine=&geo=lat,lng&radiusKm=
export async function search(params: SearchParams): Promise<SearchResponse> {
  const query: Record<string, string> = { q: params.q };
  if (params.cuisine) {
    query.cuisine = params.cuisine;
  }
  if (params.geo) {
    query.geo = `${params.geo.lat},${params.geo.lng}`;
    if (params.radiusKm != null) {
      query.radiusKm = String(params.radiusKm);
    }
  }
  const { data } = await apiClient.get<SearchResponse>('/api/search', {
    params: query,
  });
  return data;
}

// --- restaurant-service (gateway route /api/restaurants/**) ---

// GET /api/restaurants?cuisine=
export async function listRestaurants(
  cuisine?: string,
): Promise<Restaurant[]> {
  const { data } = await apiClient.get<Restaurant[]>('/api/restaurants', {
    params: cuisine ? { cuisine } : undefined,
  });
  return data;
}

// GET /api/restaurants/{id}
export async function getRestaurant(id: string): Promise<Restaurant> {
  const { data } = await apiClient.get<Restaurant>(`/api/restaurants/${id}`);
  return data;
}

// --- menu-service (gateway route /api/menu/**) ---

// GET /api/menu?restaurantId=
export async function getMenu(restaurantId: string): Promise<MenuItem[]> {
  const { data } = await apiClient.get<MenuItem[]>('/api/menu', {
    params: { restaurantId },
  });
  return data;
}

// NOTE: discovery-mfe is intentionally limited to the search, restaurant and
// menu services (PLATFORM_SPEC §1.2). It must NOT call cart-service directly —
// the cart/checkout flow is owned by checkout-mfe. The "Add" action in the menu
// UI therefore dispatches a cross-MFE event (see types.ts AddToCartEvent) that
// the shell host forwards to checkout-mfe instead of hitting /api/carts/**.
