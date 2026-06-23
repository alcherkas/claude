import { useQuery } from '@tanstack/react-query';
import { listOrders } from '../api/client';
import type { Order } from '../types';

interface OrderListProps {
  userId: string;
  onSelect: (orderId: string) => void;
}

function formatMoney(cents: number, currency: string): string {
  return new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: currency || 'USD',
  }).format(cents / 100);
}

// Lists the signed-in customer's orders. Polls every 5s so newly placed orders
// and status changes surface without a manual refresh.
export default function OrderList({ userId, onSelect }: OrderListProps) {
  const {
    data: orders,
    isLoading,
    isError,
    error,
  } = useQuery<Order[]>({
    queryKey: ['orders', userId],
    queryFn: () => listOrders(userId),
    refetchInterval: 5_000,
  });

  if (isLoading) {
    return <p className="ot-empty">Loading your orders…</p>;
  }

  if (isError) {
    return (
      <p className="ot-error">
        Could not load orders: {(error as Error)?.message ?? 'unknown error'}
      </p>
    );
  }

  if (!orders || orders.length === 0) {
    return <p className="ot-empty">You have no active orders.</p>;
  }

  return (
    <ul className="ot-order-list">
      {orders.map((order) => (
        <li key={order.id} className="ot-order-card">
          <button type="button" onClick={() => onSelect(order.id)}>
            <div className="ot-order-card__top">
              <span className="ot-order-id">#{order.id.slice(0, 8)}</span>
              <span className={`ot-badge ot-badge--${order.status.toLowerCase()}`}>
                {order.status}
              </span>
            </div>
            <div className="ot-order-card__meta">
              <span>
                {order.items.length} item{order.items.length === 1 ? '' : 's'}
              </span>
              <span>{formatMoney(order.pricing.totalCents, order.pricing.currency)}</span>
            </div>
            <time className="ot-order-card__time">
              {new Date(order.createdAt).toLocaleString()}
            </time>
          </button>
        </li>
      ))}
    </ul>
  );
}
