import { lazy, Suspense, type ComponentType, type ReactNode } from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { Layout } from './components/Layout';
import { ProtectedRoute } from './components/ProtectedRoute';
import { RemoteBoundary } from './components/RemoteBoundary';

// Federated remotes — lazily code-split so each MFE bundle loads on demand.
const DiscoveryApp = lazy<ComponentType>(() => import('discovery/App'));
const CheckoutApp = lazy<ComponentType>(() => import('checkout/App'));
const OrderTrackingApp = lazy<ComponentType>(() => import('orderTracking/App'));
const AccountApp = lazy<ComponentType>(() => import('account/App'));
const RestaurantAdminApp = lazy<ComponentType>(
  () => import('restaurantAdmin/App'),
);
const DriverPortalApp = lazy<ComponentType>(() => import('driverPortal/App'));

function RemoteSlot({
  name,
  children,
}: {
  name: string;
  children: ReactNode;
}) {
  return (
    <RemoteBoundary name={name}>
      <Suspense fallback={<div className="qb-loading">Loading {name}…</div>}>
        {children}
      </Suspense>
    </RemoteBoundary>
  );
}

export default function App() {
  return (
    <BrowserRouter>
      <Layout>
        <Routes>
          <Route
            path="/"
            element={
              <RemoteSlot name="Discovery">
                <DiscoveryApp />
              </RemoteSlot>
            }
          />
          <Route
            path="/discover/*"
            element={
              <RemoteSlot name="Discovery">
                <DiscoveryApp />
              </RemoteSlot>
            }
          />
          <Route
            path="/checkout/*"
            element={
              <RemoteSlot name="Checkout">
                <CheckoutApp />
              </RemoteSlot>
            }
          />
          <Route
            path="/orders/*"
            element={
              <RemoteSlot name="Order tracking">
                <OrderTrackingApp />
              </RemoteSlot>
            }
          />
          <Route
            path="/account/*"
            element={
              <ProtectedRoute>
                <RemoteSlot name="Account">
                  <AccountApp />
                </RemoteSlot>
              </ProtectedRoute>
            }
          />
          <Route
            path="/partner/*"
            element={
              <ProtectedRoute role="RESTAURANT_OWNER">
                <RemoteSlot name="Restaurant admin">
                  <RestaurantAdminApp />
                </RemoteSlot>
              </ProtectedRoute>
            }
          />
          <Route
            path="/driver/*"
            element={
              <ProtectedRoute role="COURIER">
                <RemoteSlot name="Driver portal">
                  <DriverPortalApp />
                </RemoteSlot>
              </ProtectedRoute>
            }
          />
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </Layout>
    </BrowserRouter>
  );
}
