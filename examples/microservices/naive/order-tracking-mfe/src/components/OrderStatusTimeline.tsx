import type { OrderStatus } from '../types';

interface OrderStatusTimelineProps {
  status: OrderStatus;
}

// The forward-progress order lifecycle (PLATFORM_SPEC §3, order-service).
// CANCELLED is terminal and rendered separately.
const FLOW: OrderStatus[] = [
  'CREATED',
  'CONFIRMED',
  'PREPARING',
  'READY',
  'PICKED_UP',
  'DELIVERED',
];

const LABELS: Record<OrderStatus, string> = {
  CREATED: 'Order placed',
  CONFIRMED: 'Confirmed',
  PREPARING: 'Preparing',
  READY: 'Ready for pickup',
  PICKED_UP: 'Picked up',
  DELIVERED: 'Delivered',
  CANCELLED: 'Cancelled',
};

export default function OrderStatusTimeline({ status }: OrderStatusTimelineProps) {
  if (status === 'CANCELLED') {
    return (
      <div className="ot-timeline ot-timeline--cancelled">
        <p>This order was cancelled.</p>
      </div>
    );
  }

  const activeIndex = FLOW.indexOf(status);

  return (
    <ol className="ot-timeline" aria-label="Order status timeline">
      {FLOW.map((step, index) => {
        const state =
          index < activeIndex ? 'done' : index === activeIndex ? 'current' : 'todo';
        return (
          <li key={step} className={`ot-timeline__step ot-timeline__step--${state}`}>
            <span className="ot-timeline__dot" aria-hidden="true" />
            <span className="ot-timeline__label">{LABELS[step]}</span>
          </li>
        );
      })}
    </ol>
  );
}
