import { useQuery } from '@tanstack/react-query';
import { getOrder } from '../api/client';
import type { Order } from '../types';
import OrderStatusTimeline from './OrderStatusTimeline';
import DeliveryTracker from './DeliveryTracker';

interface OrderTrackingPageProps {
  orderId: string;
  onBack: () => void;
}

// Detail view for a single order: the order status timeline (order-service) plus
// the live delivery tracker (delivery-service). The order itself polls every 5s
// so status transitions driven by deliveries.events surface promptly.
export default function OrderTrackingPage({ orderId, onBack }: OrderTrackingPageProps) {
  const {
    data: order,
    isLoading,
    isError,
  } = useQuery<Order>({
    queryKey: ['order', orderId],
    queryFn: () => getOrder(orderId),
    refetchInterval: 5_000,
  });

  return (
    <div className="ot-tracking-page">
      <button type="button" className="ot-back" onClick={onBack}>
        ← All orders
      </button>

      {isLoading && <p className="ot-empty">Loading order…</p>}
      {isError && <p className="ot-error">Could not load this order.</p>}

      {order && (
        <>
          <div className="ot-tracking-page__head">
            <h2>Order #{order.id.slice(0, 8)}</h2>
            <span className={`ot-badge ot-badge--${order.status.toLowerCase()}`}>
              {order.status}
            </span>
          </div>

          <section className="ot-panel">
            <h3>Status</h3>
            <OrderStatusTimeline status={order.status} />
          </section>

          <section className="ot-panel">
            <h3>Items</h3>
            <ul className="ot-items">
              {order.items.map((item) => (
                <li key={item.menuItemId}>
                  <span>
                    {item.qty}× {item.name}
                  </span>
                  <span>${((item.unitPriceCents * item.qty) / 100).toFixed(2)}</span>
                </li>
              ))}
            </ul>
          </section>

          <section className="ot-panel">
            <h3>Delivery</h3>
            <DeliveryTracker orderId={order.id} />
          </section>
        </>
      )}
    </div>
  );
}
