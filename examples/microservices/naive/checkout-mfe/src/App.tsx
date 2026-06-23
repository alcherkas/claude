import { Routes, Route } from 'react-router-dom';
import { CartView } from './components/CartView';
import { CheckoutForm } from './components/CheckoutForm';
import { PaymentForm } from './components/PaymentForm';
import { OrderSuccess } from './components/OrderSuccess';

// Exposed remote root (federation name "checkout", './App'). The shell host
// mounts this under a route prefix; the routes below are relative to that mount
// point and drive the cart → quote → order → pay → success flow.
export default function App() {
  return (
    <div className="checkout-app">
      <Routes>
        <Route path="/" element={<CartView />} />
        <Route path="/checkout" element={<CheckoutForm />} />
        <Route path="/payment/:orderId" element={<PaymentForm />} />
        <Route path="/success/:orderId" element={<OrderSuccess />} />
      </Routes>
    </div>
  );
}
