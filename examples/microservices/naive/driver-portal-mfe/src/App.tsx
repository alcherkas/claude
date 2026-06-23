import { type ReactNode, useState } from 'react';
import {
  Routes,
  Route,
  MemoryRouter,
  useInRouterContext,
} from 'react-router-dom';
import {
  QueryClient,
  QueryClientProvider,
  useQueryClient,
} from '@tanstack/react-query';
import { DriverDashboard } from './components/DriverDashboard';

// Routes owned by the driver portal feature. Mounted under the host's
// "/driver/*" slot (PLATFORM_SPEC §1.2 — shell composes the remotes); also
// resolves at "/" so the remote runs standalone on its own dev server.
function DriverPortalRoutes() {
  return (
    <div className="dp-app">
      <Routes>
        <Route index element={<DriverDashboard />} />
        <Route path="driver" element={<DriverDashboard />} />
      </Routes>
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

export default function App() {
  return (
    <EnsureQueryClient>
      <EnsureRouter>
        <DriverPortalRoutes />
      </EnsureRouter>
    </EnsureQueryClient>
  );
}
