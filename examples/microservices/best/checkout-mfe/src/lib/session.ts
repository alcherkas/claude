import { TOKEN_STORAGE_KEY } from '../api/client';

interface JwtClaims {
  sub?: string;
  userId?: string;
  role?: string;
  exp?: number;
}

function decodeJwt(token: string): JwtClaims | null {
  try {
    const payload = token.split('.')[1];
    if (!payload) return null;
    const json = atob(payload.replace(/-/g, '+').replace(/_/g, '/'));
    return JSON.parse(json) as JwtClaims;
  } catch {
    return null;
  }
}

// The shell host issues the JWT (identity-service, HS256). The checkout remote
// reads the persisted token to discover the current user id for cart/order
// calls. The gateway re-validates and forwards X-User-Id downstream.
export function currentUserId(): string | null {
  const token = localStorage.getItem(TOKEN_STORAGE_KEY);
  if (!token) return null;
  const claims = decodeJwt(token);
  return claims?.userId ?? claims?.sub ?? null;
}
