import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { getMe, listOrders } from '../api/client';
import type { Order } from '../types';
import { formatDateTime, formatMoney } from '../format';
import { ReviewForm } from './ReviewForm';

// Past orders — order-service GET /api/orders?userId=. Delivered orders can be
// reviewed inline (review-service verifies the order is DELIVERED and owned by
// the user, per PLATFORM_SPEC §3).
export function OrderHistory() {
  const [reviewing, setReviewing] = useState<Order | null>(null);

  const { data: user } = useQuery({ queryKey: ['me'], queryFn: getMe });
  const userId = user?.id;

  const {
    data: orders,
    isLoading,
    isError,
  } = useQuery({
    queryKey: ['orders', userId],
    queryFn: () => listOrders(userId as string),
    enabled: Boolean(userId),
  });

  if (isLoading || !userId) {
    return <div className="acct-card acct-muted">Loading orders…</div>;
  }

  if (isError || !orders) {
    return (
      <div className="acct-card acct-error">Could not load your orders.</div>
    );
  }

  if (orders.length === 0) {
    return (
      <div className="acct-card acct-muted">
        You have not placed any orders yet.
      </div>
    );
  }

  return (
    <section className="acct-orders">
      <ul className="acct-orders__list">
        {orders.map((order) => (
          <li key={order.id} className="acct-card acct-order">
            <div className="acct-order__head">
              <div>
                <span className="acct-order__id">
                  Order #{order.id.slice(0, 8)}
                </span>
                <span className="acct-order__date">
                  {formatDateTime(order.createdAt)}
                </span>
              </div>
              <span
                className={`acct-status acct-status--${order.status.toLowerCase()}`}
              >
                {order.status}
              </span>
            </div>

            <ul className="acct-order__items">
              {order.items.map((item) => (
                <li key={item.menuItemId}>
                  <span>
                    {item.qty}× {item.name}
                  </span>
                  <span>
                    {formatMoney(
                      item.unitPriceCents * item.qty,
                      order.pricing.currency,
                    )}
                  </span>
                </li>
              ))}
            </ul>

            <div className="acct-order__foot">
              <strong>
                Total{' '}
                {formatMoney(order.pricing.totalCents, order.pricing.currency)}
                {order.pricing.tipCents > 0 && (
                  <span className="acct-order__tip">
                    {' '}
                    (incl.{' '}
                    {formatMoney(
                      order.pricing.tipCents,
                      order.pricing.currency,
                    )}{' '}
                    tip)
                  </span>
                )}
              </strong>
              {order.status === 'DELIVERED' && (
                <button
                  type="button"
                  className="acct-btn acct-btn--ghost"
                  onClick={() => setReviewing(order)}
                >
                  Leave a review
                </button>
              )}
            </div>
          </li>
        ))}
      </ul>

      {reviewing && (
        <ReviewForm
          order={reviewing}
          onClose={() => setReviewing(null)}
        />
      )}
    </section>
  );
}
