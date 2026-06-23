import axios, { type AxiosInstance } from 'axios';
import type { Delivery, Order, Tracking } from '../types';

// All traffic enters through the QuickBite API gateway (PLATFORM_SPEC §2.6, §4).
// The base URL is configurable per environment; locally it defaults to the
// gateway on :8080.
const baseURL =
  (import.meta.env.VITE_GATEWAY_URL as string | undefined) ?? 'http://localhost:8080';

export const apiClient: AxiosInstance = axios.create({
  baseURL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Attach the JWT issued by identity-service (stored by the shell on login) as a
// bearer token on every request. The gateway validates it and forwards
// X-User-Id / X-User-Role downstream.
apiClient.interceptors.request.use((config) => {
  const token = localStorage.getItem('qb_token');
  if (token) {
    config.headers = config.headers ?? {};
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// ---- order-service (gateway prefix /api/orders) ----

export async function listOrders(userId: string): Promise<Order[]> {
  const { data } = await apiClient.get<Order[]>('/api/orders', {
    params: { userId },
  });
  return data;
}

export async function getOrder(id: string): Promise<Order> {
  const { data } = await apiClient.get<Order>(`/api/orders/${id}`);
  return data;
}

// ---- delivery-service (gateway prefix /api/deliveries) ----

export async function getDelivery(orderId: string): Promise<Delivery | null> {
  const { data } = await apiClient.get<Delivery[]>('/api/deliveries', {
    params: { orderId },
  });
  // The delivery endpoint returns the (at most one) delivery for an order.
  return data.length > 0 ? data[0] : null;
}

export async function getTracking(deliveryId: string): Promise<Tracking> {
  const { data } = await apiClient.get<Tracking>(
    `/api/deliveries/${deliveryId}/tracking`,
  );
  return data;
}
