import axios, { type AxiosInstance } from 'axios';
import type {
  AddCreditRequest,
  Order,
  Review,
  SubmitReviewRequest,
  User,
  Wallet,
} from '../types';

// Shared key for the persisted JWT — kept in sync with the shell host's
// AuthProvider so the remote reuses the same session token.
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

// --- identity-service (gateway route /api/users/**) -------------------------

export async function getMe(): Promise<User> {
  const { data } = await apiClient.get<User>('/api/users/me');
  return data;
}

// --- wallet-service (gateway route /api/wallets/**) -------------------------

export async function getWallet(userId: string): Promise<Wallet> {
  const { data } = await apiClient.get<Wallet>(
    `/api/wallets/${encodeURIComponent(userId)}`,
  );
  return data;
}

export async function addCredit(
  userId: string,
  body: AddCreditRequest,
): Promise<Wallet> {
  const { data } = await apiClient.post<Wallet>(
    `/api/wallets/${encodeURIComponent(userId)}/credits`,
    body,
  );
  return data;
}

// --- order-service (gateway route /api/orders/**) ---------------------------

export async function listOrders(userId: string): Promise<Order[]> {
  const { data } = await apiClient.get<Order[]>('/api/orders', {
    params: { userId },
  });
  return data;
}

// --- review-service (gateway route /api/reviews/**) -------------------------

export async function myReviews(userId: string): Promise<Review[]> {
  const { data } = await apiClient.get<Review[]>('/api/reviews', {
    params: { userId },
  });
  return data;
}

export async function submitReview(
  body: SubmitReviewRequest,
): Promise<Review> {
  const { data } = await apiClient.post<Review>('/api/reviews', body);
  return data;
}
