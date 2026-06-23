import axios, { type AxiosInstance } from 'axios';
import type {
  AdvanceDeliveryRequest,
  Delivery,
  DeliveryStatus,
  Driver,
  LocationPing,
  SetAvailabilityRequest,
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

// --- driver-service (gateway route /api/drivers/**) ---

// GET /api/drivers/{id}
export async function getDriver(id: string): Promise<Driver> {
  const { data } = await apiClient.get<Driver>(`/api/drivers/${id}`);
  return data;
}

// PATCH /api/drivers/{id}/availability
export async function setAvailability(
  id: string,
  available: boolean,
): Promise<Driver> {
  const body: SetAvailabilityRequest = { available };
  const { data } = await apiClient.patch<Driver>(
    `/api/drivers/${id}/availability`,
    body,
  );
  return data;
}

// POST /api/drivers/{id}/location
export async function pingLocation(
  id: string,
  ping: LocationPing,
): Promise<void> {
  await apiClient.post(`/api/drivers/${id}/location`, ping);
}

// --- delivery-service (gateway route /api/deliveries/**) ---

// GET /api/deliveries?driverId=   — deliveries assigned to (or assignable by)
// a given courier. Pass no driverId to list the unassigned/available pool.
export async function listDeliveries(driverId?: string): Promise<Delivery[]> {
  const { data } = await apiClient.get<Delivery[]>('/api/deliveries', {
    params: driverId ? { driverId } : undefined,
  });
  return data;
}

// PATCH /api/deliveries/{id}/status
export async function advanceDelivery(
  id: string,
  status: DeliveryStatus,
): Promise<Delivery> {
  const body: AdvanceDeliveryRequest = { status };
  const { data } = await apiClient.patch<Delivery>(
    `/api/deliveries/${id}/status`,
    body,
  );
  return data;
}
