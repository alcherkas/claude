// Shared host types for QuickBite shell.

export type UserRole = 'CUSTOMER' | 'RESTAURANT_OWNER' | 'COURIER' | 'ADMIN';

export interface User {
  id: string;
  email: string;
  fullName: string;
  role: UserRole;
  createdAt: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  email: string;
  password: string;
  fullName: string;
  role: UserRole;
}

// identity-service /api/auth/login & /register response.
export interface AuthResponse {
  token: string;
  user: User;
}

// What the AuthProvider persists in localStorage and exposes via context.
export interface AuthSession {
  token: string;
  user: User;
}
