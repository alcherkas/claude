import { NavLink, Routes, Route, Navigate } from 'react-router-dom';
import { ProfileCard } from './components/ProfileCard';
import { WalletPanel } from './components/WalletPanel';
import { OrderHistory } from './components/OrderHistory';
import { ReviewList } from './components/ReviewList';

// Federated remote root (exposed as './App'). Rendered inside the shell host's
// <BrowserRouter> at /account/*, so all routes here are relative. The host
// guards this remote behind authentication.
export default function App() {
  return (
    <div className="acct">
      <header className="acct__header">
        <h1>Your account</h1>
        <nav className="acct__tabs">
          <NavLink to="profile" className={navClass}>
            Profile
          </NavLink>
          <NavLink to="wallet" className={navClass}>
            Wallet
          </NavLink>
          <NavLink to="orders" className={navClass}>
            Orders
          </NavLink>
          <NavLink to="reviews" className={navClass}>
            Reviews
          </NavLink>
        </nav>
      </header>

      <main className="acct__body">
        <Routes>
          <Route index element={<Navigate to="profile" replace />} />
          <Route path="profile" element={<ProfileCard />} />
          <Route path="wallet" element={<WalletPanel />} />
          <Route path="orders" element={<OrderHistory />} />
          <Route path="reviews" element={<ReviewList />} />
          <Route path="*" element={<Navigate to="profile" replace />} />
        </Routes>
      </main>
    </div>
  );
}

function navClass({ isActive }: { isActive: boolean }): string {
  return isActive ? 'acct__tab acct__tab--active' : 'acct__tab';
}
