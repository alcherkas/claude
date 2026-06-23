import { Route, Routes, useParams, useNavigate } from 'react-router-dom';
import OrderList from './components/OrderList';
import OrderTrackingPage from './components/OrderTrackingPage';

// Resolve the current customer id. When mounted inside the shell host the user
// id is stamped into localStorage at login (alongside the JWT). Fall back to a
// demo id so the remote is usable standalone on :3003.
function currentUserId(): string {
  return localStorage.getItem('qb_user_id') ?? 'demo-user';
}

function OrdersRoute() {
  const navigate = useNavigate();
  return (
    <OrderList
      userId={currentUserId()}
      onSelect={(orderId) => navigate(`/tracking/${orderId}`)}
    />
  );
}

function TrackingRoute() {
  const { orderId } = useParams<{ orderId: string }>();
  const navigate = useNavigate();
  if (!orderId) {
    return <p className="ot-empty">No order selected.</p>;
  }
  return <OrderTrackingPage orderId={orderId} onBack={() => navigate('/')} />;
}

// Exposed as the federation remote './App' (federation name "orderTracking").
// The host mounts this under its own Router/QueryClient; standalone it is
// wrapped by bootstrap.tsx.
export default function App() {
  return (
    <section className="ot-root">
      <header className="ot-header">
        <h1>Order Tracking</h1>
        <p>Live order &amp; courier status</p>
      </header>
      <Routes>
        <Route path="/" element={<OrdersRoute />} />
        <Route path="/tracking/:orderId" element={<TrackingRoute />} />
        <Route path="*" element={<OrdersRoute />} />
      </Routes>
    </section>
  );
}
