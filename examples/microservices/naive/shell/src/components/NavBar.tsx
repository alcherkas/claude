import { NavLink } from 'react-router-dom';
import { useAuth } from './AuthProvider';

interface NavBarProps {
  onLoginClick: () => void;
}

export function NavBar({ onLoginClick }: NavBarProps) {
  const { user, isAuthenticated, hasRole, logout } = useAuth();

  return (
    <header className="qb-nav">
      <NavLink to="/" className="qb-nav__brand">
        QuickBite
      </NavLink>

      <nav className="qb-nav__links">
        <NavLink to="/discover">Discover</NavLink>
        <NavLink to="/checkout">Checkout</NavLink>
        <NavLink to="/orders">Orders</NavLink>
        {isAuthenticated && <NavLink to="/account">Account</NavLink>}
        {hasRole('RESTAURANT_OWNER') && (
          <NavLink to="/partner">Partner</NavLink>
        )}
        {hasRole('COURIER') && <NavLink to="/driver">Driver</NavLink>}
      </nav>

      <div className="qb-nav__auth">
        {isAuthenticated && user ? (
          <>
            <span className="qb-nav__user" title={user.email}>
              {user.fullName}
            </span>
            <button type="button" className="qb-btn qb-btn--ghost" onClick={logout}>
              Log out
            </button>
          </>
        ) : (
          <button type="button" className="qb-btn" onClick={onLoginClick}>
            Log in
          </button>
        )}
      </div>
    </header>
  );
}
