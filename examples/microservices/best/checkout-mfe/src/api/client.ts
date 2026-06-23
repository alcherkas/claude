import axios, { type AxiosInstance } from 'axios';
import type {
  Cart,
  Order,
  Payment,
  PaymentRequest,
  PlaceOrderRequest,
  PromoValidation,
  Quote,
  QuoteRequest,
  UpdateCartItemRequest,
  Wallet,
} from '../types';

// Single shared key for the persisted JWT — kept in sync with the shell host's
// AuthProvider so a remote loaded inside the shell reuses the same session.
export const TOKEN_STORAGE_KEY = 'quickbite.token';

// All external traffic enters at the gateway (PLATFORM_SPEC §2.6).
export const GATEWAY_URL: string =
  import.meta.env.VITE_GATEWAY_URL || 'http://localhost:8080';

export const apiClient: AxiosInstance = axios.create({
  baseURL: GATEWAY_URL,
  headers: { 'Content-Type': 'application/json' },
});

// Attach the bearer token from localStorage to every outgoing request.
apiClient.interceptors.request.use((config) => {
  const token = localStorage.getItem(TOKEN_STORAGE_KEY);
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// On a 401 the session is stale — clear it so the shell drops to logged-out.
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error?.response?.status === 401) {
      localStorage.removeItem(TOKEN_STORAGE_KEY);
    }
    return Promise.reject(error);
  },
);

// --- cart-service (gateway route /api/carts/**) -----------------------------

export async function getCart(userId: string): Promise<Cart> {
  const { data } = await apiClient.get<Cart>(
    `/api/carts/${encodeURIComponent(userId)}`,
  );
  return data;
}

export async function updateItem(
  userId: string,
  menuItemId: string,
  body: UpdateCartItemRequest,
): Promise<Cart> {
  const { data } = await apiClient.put<Cart>(
    `/api/carts/${encodeURIComponent(userId)}/items/${encodeURIComponent(menuItemId)}`,
    body,
  );
  return data;
}

export async function removeItem(
  userId: string,
  menuItemId: string,
): Promise<Cart> {
  const { data } = await apiClient.delete<Cart>(
    `/api/carts/${encodeURIComponent(userId)}/items/${encodeURIComponent(menuItemId)}`,
  );
  return data;
}

// --- promotion-service (gateway route /api/promotions/**) -------------------

export async function validatePromo(
  code: string,
  subtotalCents: number,
): Promise<PromoValidation> {
  const { data } = await apiClient.get<PromoValidation>(
    '/api/promotions/validate',
    { params: { code, subtotalCents } },
  );
  return data;
}

// --- pricing-service (gateway route /api/pricing/**) ------------------------

export async function quote(body: QuoteRequest): Promise<Quote> {
  const { data } = await apiClient.post<Quote>('/api/pricing/quote', body);
  return data;
}

// --- order-service (gateway route /api/orders/**) ---------------------------

export async function placeOrder(body: PlaceOrderRequest): Promise<Order> {
  const { data } = await apiClient.post<Order>('/api/orders', body);
  return data;
}

// --- payment-service (gateway route /api/payments/**) -----------------------

export async function pay(body: PaymentRequest): Promise<Payment> {
  const { data } = await apiClient.post<Payment>('/api/payments', body);
  return data;
}

// --- wallet-service (gateway route /api/wallets/**) -------------------------

export async function getWallet(userId: string): Promise<Wallet> {
  const { data } = await apiClient.get<Wallet>(
    `/api/wallets/${encodeURIComponent(userId)}`,
  );
  return data;
}
