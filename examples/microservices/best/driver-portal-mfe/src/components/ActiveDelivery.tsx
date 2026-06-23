import { useEffect } from 'react';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { advanceDelivery, pingLocation } from '../api/client';
import type { Delivery } from '../types';
import {
  formatCents,
  nextActionLabel,
  nextStatus,
  statusLabel,
} from '../delivery-flow';

// The courier's single in-progress delivery. Drives the status forward and
// pings the driver's location to driver-service while EN_ROUTE_* so the
// customer's order-tracking-mfe sees live movement (deliveries.events).
export function ActiveDelivery({
  delivery,
  driverId,
}: {
  delivery: Delivery;
  driverId: string;
}) {
  const queryClient = useQueryClient();

  const advance = useMutation({
    mutationFn: (status: Delivery['status']) =>
      advanceDelivery(delivery.id, status),
    onSuccess: (updated) => {
      queryClient.setQueryData<Delivery[]>(
        ['deliveries', 'assigned', driverId],
        (prev) =>
          prev?.map((d) => (d.id === updated.id ? updated : d)) ?? [updated],
      );
      void queryClient.invalidateQueries({ queryKey: ['deliveries'] });
      void queryClient.invalidateQueries({ queryKey: ['driver', driverId] });
    },
  });

  const enRoute =
    delivery.status === 'EN_ROUTE_TO_PICKUP' ||
    delivery.status === 'EN_ROUTE_TO_CUSTOMER';

  // While en route, emit a periodic location ping. The browser geolocation API
  // feeds driver-service; we fall back to the last known tracking point.
  useEffect(() => {
    if (!enRoute) return;

    let cancelled = false;
    const send = () => {
      const fallback = delivery.trackingPoints.at(-1);
      const emit = (lat: number, lng: number) => {
        if (cancelled) return;
        void pingLocation(driverId, {
          lat,
          lng,
          recordedAt: new Date().toISOString(),
        });
      };
      if (navigator.geolocation) {
        navigator.geolocation.getCurrentPosition(
          (pos) => emit(pos.coords.latitude, pos.coords.longitude),
          () => {
            if (fallback) emit(fallback.lat, fallback.lng);
          },
          { enableHighAccuracy: true, maximumAge: 5_000, timeout: 5_000 },
        );
      } else if (fallback) {
        emit(fallback.lat, fallback.lng);
      }
    };

    send();
    const handle = window.setInterval(send, 15_000);
    return () => {
      cancelled = true;
      window.clearInterval(handle);
    };
  }, [enRoute, driverId, delivery.trackingPoints]);

  const next = nextStatus(delivery.status);
  const label = nextActionLabel(delivery.status);

  return (
    <div className="dp-card">
      <div className="dp-row">
        <h3>Active delivery</h3>
        <span className="dp-badge dp-badge--busy">
          {statusLabel(delivery.status)}
        </span>
      </div>

      <p>
        <strong>{formatCents(delivery.payoutCents)}</strong>{' '}
        <span className="dp-muted">order #{delivery.orderId}</span>
      </p>
      <div className="dp-muted">Pickup: {delivery.pickupAddress}</div>
      <div className="dp-muted">
        Drop-off: {delivery.dropoffAddress} ({delivery.customerName})
      </div>

      <div className="dp-row" style={{ marginTop: 12 }}>
        <button
          type="button"
          className="dp-btn dp-btn--danger"
          disabled={advance.isPending}
          onClick={() => advance.mutate('FAILED')}
        >
          Report problem
        </button>
        {next && label && (
          <button
            type="button"
            className="dp-btn dp-btn--primary"
            disabled={advance.isPending}
            onClick={() => advance.mutate(next)}
          >
            {label}
          </button>
        )}
      </div>

      {advance.isError && (
        <p className="dp-error">Could not update the delivery. Try again.</p>
      )}
    </div>
  );
}
