import { Link, useParams } from 'react-router-dom';
import { useQueryClient } from '@tanstack/react-query';
import type { Order } from '../types';
import { formatCents } from '../lib/money';

// Terminal step of the checkout flow. The OrderCreated / PaymentCaptured events
// (orders.events / payments.events) drive the rest of the journey; from here the
// customer moves to order-tracking-mfe.
export function OrderSuccess() {
  const { orderId } = useParams<{ orderId: string }>();
  const queryClient = useQueryClient();
  const order = queryClient.getQueryData<Order>(['order', orderId ?? '']);

  return (
    <section className="panel success">
      <h2>Order confirmed 🎉</h2>
      <p>
        Thanks! Your order <strong>#{orderId}</strong> has been placed and paid.
      </p>
      {order && order.pricing.tipCents > 0 && (
        <p className="success__tip">
          Includes a courier tip of{' '}
          <strong>{formatCents(order.pricing.tipCents)}</strong>
        </p>
      )}
      {order && (
        <p className="success__total">
          Total charged: <strong>{formatCents(order.pricing.totalCents)}</strong>
        </p>
      )}
      <p>
        You can follow your courier in real time from order tracking.
      </p>
      <Link className="primary" to="/">
        Back to cart
      </Link>
    </section>
  );
}
