import { useQuery } from '@tanstack/react-query';
import { getDelivery, getTracking } from '../api/client';
import type { Delivery, DeliveryStatus, Tracking } from '../types';
import LiveMap from './LiveMap';

interface DeliveryTrackerProps {
  orderId: string;
}

const STATUS_LABELS: Record<DeliveryStatus, string> = {
  PENDING: 'Waiting for a courier',
  ASSIGNED: 'Courier assigned',
  EN_ROUTE_TO_PICKUP: 'Courier heading to restaurant',
  PICKED_UP: 'Order picked up',
  EN_ROUTE_TO_CUSTOMER: 'On the way to you',
  DELIVERED: 'Delivered',
  FAILED: 'Delivery failed',
};

// Resolves the delivery for an order, then polls its tracking feed. Both queries
// poll every 4s (TanStack Query refetchInterval) for a near-live view.
export default function DeliveryTracker({ orderId }: DeliveryTrackerProps) {
  const {
    data: delivery,
    isLoading: deliveryLoading,
    isError: deliveryError,
  } = useQuery<Delivery | null>({
    queryKey: ['delivery', orderId],
    queryFn: () => getDelivery(orderId),
    refetchInterval: 4_000,
  });

  const deliveryId = delivery?.id;

  const { data: tracking } = useQuery<Tracking>({
    queryKey: ['tracking', deliveryId],
    queryFn: () => getTracking(deliveryId as string),
    enabled: Boolean(deliveryId),
    refetchInterval: 4_000,
  });

  if (deliveryLoading) {
    return <p className="ot-empty">Looking up delivery…</p>;
  }

  if (deliveryError) {
    return <p className="ot-error">Could not load delivery information.</p>;
  }

  if (!delivery) {
    return (
      <div className="ot-delivery">
        <p className="ot-empty">No courier assigned yet. Hang tight!</p>
      </div>
    );
  }

  // Prefer the live tracking feed; fall back to the points embedded on the
  // delivery record before the first tracking poll resolves.
  const status = tracking?.status ?? delivery.status;
  const points = tracking?.points ?? delivery.trackingPoints;
  const lastUpdated = tracking?.updatedAt;

  return (
    <div className="ot-delivery">
      <div className="ot-delivery__header">
        <span className={`ot-badge ot-badge--${status.toLowerCase()}`}>{status}</span>
        <span className="ot-delivery__label">{STATUS_LABELS[status]}</span>
      </div>
      {delivery.driverId && (
        <p className="ot-delivery__driver">Courier #{delivery.driverId.slice(0, 8)}</p>
      )}
      <LiveMap points={points} />
      {lastUpdated && (
        <time className="ot-delivery__updated">
          Updated {new Date(lastUpdated).toLocaleTimeString()}
        </time>
      )}
    </div>
  );
}
