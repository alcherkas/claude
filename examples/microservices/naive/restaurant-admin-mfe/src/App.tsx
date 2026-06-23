import { type ReactNode, useState } from 'react';
import {
  MemoryRouter,
  NavLink,
  Route,
  Routes,
  useInRouterContext,
} from 'react-router-dom';
import {
  QueryClient,
  QueryClientProvider,
  useQueryClient,
} from '@tanstack/react-query';
import {
  SelectedRestaurantContext,
  type SelectedRestaurant,
} from './lib/selectedRestaurant';
import { RestaurantDashboard } from './components/RestaurantDashboard';
import { MenuEditor } from './components/MenuEditor';
import { IncomingOrders } from './components/IncomingOrders';
import { PromotionManager } from './components/PromotionManager';
import './styles.css';

// Routes owned by the restaurant-admin feature. Mounted under the host's
// "/admin/*" slot (PLATFORM_SPEC §1.2 — shell composes the remotes). The base
// "index" route also resolves so the remote works standalone at "/".
function AdminNav() {
  return (
    <nav className="ra-nav">
      <NavLink to="" end>
        Dashboard
      </NavLink>
      <NavLink to="menu">Menu</NavLink>
      <NavLink to="orders">Incoming Orders</NavLink>
      <NavLink to="promotions">Promotions</NavLink>
    </nav>
  );
}

function AdminRoutes() {
  return (
    <div className="ra-app">
      <h1>Restaurant Admin</h1>
      <div className="ra-layout">
        <AdminNav />
        <main>
          <Routes>
            <Route index element={<RestaurantDashboard />} />
            <Route path="menu" element={<MenuEditor />} />
            <Route path="orders" element={<IncomingOrders />} />
            <Route path="promotions" element={<PromotionManager />} />
            {/* Standalone-dev aliases under /admin/*. */}
            <Route path="admin" element={<RestaurantDashboard />} />
            <Route path="admin/menu" element={<MenuEditor />} />
            <Route path="admin/orders" element={<IncomingOrders />} />
            <Route path="admin/promotions" element={<PromotionManager />} />
          </Routes>
        </main>
      </div>
    </div>
  );
}

// When consumed by the shell host, both a Router and a QueryClient already
// exist (shared singletons). When something renders this remote outside those
// providers we fall back to our own so the exposed component is self-sufficient.
function EnsureRouter({ children }: { children: ReactNode }) {
  const inRouter = useInRouterContext();
  if (inRouter) return <>{children}</>;
  return <MemoryRouter>{children}</MemoryRouter>;
}

function EnsureQueryClient({ children }: { children: ReactNode }) {
  const [fallback] = useState(
    () =>
      new QueryClient({
        defaultOptions: {
          queries: { retry: 1, staleTime: 15_000, refetchOnWindowFocus: false },
        },
      }),
  );
  let hasClient = true;
  try {
    useQueryClient();
  } catch {
    hasClient = false;
  }
  if (hasClient) return <>{children}</>;
  return (
    <QueryClientProvider client={fallback}>{children}</QueryClientProvider>
  );
}

// Holds the "active restaurant" the admin is currently managing.
function SelectedRestaurantProvider({ children }: { children: ReactNode }) {
  const [restaurantId, setRestaurantId] = useState<string | null>(null);
  const value: SelectedRestaurant = { restaurantId, setRestaurantId };
  return (
    <SelectedRestaurantContext.Provider value={value}>
      {children}
    </SelectedRestaurantContext.Provider>
  );
}

export default function App() {
  return (
    <EnsureQueryClient>
      <EnsureRouter>
        <SelectedRestaurantProvider>
          <AdminRoutes />
        </SelectedRestaurantProvider>
      </EnsureRouter>
    </EnsureQueryClient>
  );
}
