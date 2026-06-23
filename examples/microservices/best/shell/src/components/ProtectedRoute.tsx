import { useState, type ReactNode } from 'react';
import { useAuth } from './AuthProvider';
import { LoginDialog } from './LoginDialog';
import type { UserRole } from '../types';

interface ProtectedRouteProps {
  role?: UserRole;
  children: ReactNode;
}

// Gates a route on authentication and (optionally) a specific role.
// Unauthenticated users get a login prompt; authenticated users lacking the
// required role get a 403-style message instead of the remote.
export function ProtectedRoute({ role, children }: ProtectedRouteProps) {
  const { isAuthenticated, user } = useAuth();
  const [loginOpen, setLoginOpen] = useState(false);

  if (!isAuthenticated) {
    return (
      <div className="qb-gate">
        <h2>Sign in required</h2>
        <p>You need to be logged in to view this area.</p>
        <button type="button" className="qb-btn" onClick={() => setLoginOpen(true)}>
          Log in
        </button>
        <LoginDialog open={loginOpen} onClose={() => setLoginOpen(false)} />
      </div>
    );
  }

  if (role && user?.role !== role) {
    return (
      <div className="qb-gate">
        <h2>Access denied</h2>
        <p>
          This area requires the <strong>{role.replace('_', ' ')}</strong> role.
        </p>
      </div>
    );
  }

  return <>{children}</>;
}
