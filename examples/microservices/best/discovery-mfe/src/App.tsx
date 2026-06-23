import {
  Routes,
  Route,
  useInRouterContext,
  MemoryRouter,
} from 'react-router-dom';
import {
  QueryClient,
  QueryClientProvider,
} from '@tanstack/react-query';
import { type ReactNode, useState } from 'react';
import { DiscoverPage } from './components/DiscoverPage';
import { RestaurantDetail } from './components/RestaurantDetail';

// Routes owned by the discovery feature. Mounted under the host's "/" and
// "/discover/*" slots (PLATFORM_SPEC §1.2 — shell composes the remotes).
function DiscoveryRoutes() {
  return (
    <div className="dc-app">
      <Routes>
        <Route index element={<DiscoverPage />} />
        <Route
          path="restaurants/:restaurantId"
          element={<RestaurantDetail />}
        />
        {/* Standalone-dev alias so the feature also works at /discover/*. */}
        <Route path="discover" element={<DiscoverPage />} />
        <Route
          path="discover/restaurants/:restaurantId"
          element={<RestaurantDetail />}
        />
      </Routes>
    </div>
  );
}

// When consumed by the shell host a Router already exists (the host wraps every
// remote slot in <BrowserRouter>). When something renders this remote outside a
// router we fall back to a MemoryRouter so the exposed component is
// self-sufficient.
function EnsureRouter({ children }: { children: ReactNode }) {
  const inRouter = useInRouterContext();
  if (inRouter) return <>{children}</>;
  return <MemoryRouter>{children}</MemoryRouter>;
}

// Each MFE owns a QueryClient instance (the react-query *library* is the shared
// singleton, the client is not). Nesting a QueryClientProvider is safe: the
// nearest provider wins, and when the host already supplies one this simply
// shadows it for the discovery subtree with identical defaults.
export default function App() {
  const [queryClient] = useState(
    () =>
      new QueryClient({
        defaultOptions: {
          queries: {
            retry: 1,
            staleTime: 30_000,
            refetchOnWindowFocus: false,
          },
        },
      }),
  );

  return (
    <QueryClientProvider client={queryClient}>
      <EnsureRouter>
        <DiscoveryRoutes />
      </EnsureRouter>
    </QueryClientProvider>
  );
}
