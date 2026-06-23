import axios, { type AxiosInstance } from 'axios';
import type {
  AuthResponse,
  LoginRequest,
  RegisterRequest,
  User,
} from '../types';

// Single shared key for the persisted JWT. Kept in sync with AuthProvider.
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

// On a 401 the session is stale — clear it so the UI drops to logged-out state.
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error?.response?.status === 401) {
      localStorage.removeItem(TOKEN_STORAGE_KEY);
    }
    return Promise.reject(error);
  },
);

// --- identity-service endpoints (gateway route /api/auth/**, /api/users/**) ---

export async function login(body: LoginRequest): Promise<AuthResponse> {
  const { data } = await apiClient.post<AuthResponse>('/api/auth/login', body);
  return data;
}

export async function register(body: RegisterRequest): Promise<AuthResponse> {
  const { data } = await apiClient.post<AuthResponse>(
    '/api/auth/register',
    body,
  );
  return data;
}

export async function fetchCurrentUser(): Promise<User> {
  const { data } = await apiClient.get<User>('/api/auth/me');
  return data;
}
