import {
  createContext,
  useCallback,
  useContext,
  useMemo,
  useState,
  type ReactNode,
} from 'react';
import {
  login as loginRequest,
  register as registerRequest,
  TOKEN_STORAGE_KEY,
} from '../api/client';
import type {
  AuthSession,
  LoginRequest,
  RegisterRequest,
  User,
  UserRole,
} from '../types';

const USER_STORAGE_KEY = 'quickbite.user';

interface AuthContextValue {
  user: User | null;
  token: string | null;
  isAuthenticated: boolean;
  hasRole: (role: UserRole) => boolean;
  login: (body: LoginRequest) => Promise<User>;
  register: (body: RegisterRequest) => Promise<User>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

function readSession(): AuthSession | null {
  const token = localStorage.getItem(TOKEN_STORAGE_KEY);
  const rawUser = localStorage.getItem(USER_STORAGE_KEY);
  if (!token || !rawUser) return null;
  try {
    return { token, user: JSON.parse(rawUser) as User };
  } catch {
    return null;
  }
}

function persistSession(session: AuthSession): void {
  localStorage.setItem(TOKEN_STORAGE_KEY, session.token);
  localStorage.setItem(USER_STORAGE_KEY, JSON.stringify(session.user));
}

function clearSession(): void {
  localStorage.removeItem(TOKEN_STORAGE_KEY);
  localStorage.removeItem(USER_STORAGE_KEY);
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [session, setSession] = useState<AuthSession | null>(() =>
    readSession(),
  );

  const login = useCallback(async (body: LoginRequest): Promise<User> => {
    const res = await loginRequest(body);
    const next: AuthSession = { token: res.token, user: res.user };
    persistSession(next);
    setSession(next);
    return res.user;
  }, []);

  const register = useCallback(
    async (body: RegisterRequest): Promise<User> => {
      const res = await registerRequest(body);
      const next: AuthSession = { token: res.token, user: res.user };
      persistSession(next);
      setSession(next);
      return res.user;
    },
    [],
  );

  const logout = useCallback(() => {
    clearSession();
    setSession(null);
  }, []);

  const hasRole = useCallback(
    (role: UserRole) => session?.user.role === role,
    [session],
  );

  const value = useMemo<AuthContextValue>(
    () => ({
      user: session?.user ?? null,
      token: session?.token ?? null,
      isAuthenticated: Boolean(session),
      hasRole,
      login,
      register,
      logout,
    }),
    [session, hasRole, login, register, logout],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext);
  if (!ctx) {
    throw new Error('useAuth must be used within an <AuthProvider>');
  }
  return ctx;
}
