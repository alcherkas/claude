import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { advanceDelivery, listDeliveries } from '../api/client';
import type { Delivery } from '../types';
import { formatCents } from '../delivery-flow';

// Pool of unassigned deliveries a courier can accept. Accepting moves the
// delivery from PENDING -> ASSIGNED via PATCH /api/deliveries/{id}/status; the
// delivery-service binds it to the available driver.
export function AvailableDeliveries({ driverId }: { driverId: string }) {
  const queryClient = useQueryClient();

  const { data, isLoading, isError } = useQuery({
    queryKey: ['deliveries', 'pool'],
    queryFn: () => listDeliveries(),
    refetchInterval: 10_000,
    // Only PENDING deliveries are claimable; the service may return others.
    select: (deliveries) =>
      deliveries.filter((d) => d.status === 'PENDING' && d.driverId == null),
  });

  const accept = useMutation({
    mutationFn: (delivery: Delivery) =>
      advanceDelivery(delivery.id, 'ASSIGNED'),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['deliveries'] });
      void queryClient.invalidateQueries({ queryKey: ['driver', driverId] });
    },
  });

  if (isLoading) return <div className="dp-card dp-muted">Loading offers…</div>;
  if (isError)
    return <div className="dp-card dp-error">Could not load available deliveries.</div>;

  const offers = data ?? [];

  return (
    <div className="dp-card">
      <h3>Available deliveries</h3>
      {offers.length === 0 ? (
        <p className="dp-muted">No offers right now. Stay online for more.</p>
      ) : (
        <ul className="dp-list">
          {offers.map((d) => (
            <li key={d.id} className="dp-row">
              <div>
                <strong>{formatCents(d.payoutCents)}</strong>{' '}
                <span className="dp-muted">order #{d.orderId}</span>
                <div className="dp-muted">
                  {d.pickupAddress} → {d.dropoffAddress}
                </div>
              </div>
              <button
                type="button"
                className="dp-btn dp-btn--primary"
                disabled={accept.isPending}
                onClick={() => accept.mutate(d)}
              >
                Accept
              </button>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
