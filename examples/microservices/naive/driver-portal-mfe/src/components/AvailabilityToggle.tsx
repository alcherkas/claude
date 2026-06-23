import { useMutation, useQueryClient } from '@tanstack/react-query';
import { setAvailability } from '../api/client';
import type { Driver } from '../types';

const STATUS_BADGE: Record<Driver['status'], string> = {
  AVAILABLE: 'dp-badge dp-badge--available',
  ON_DELIVERY: 'dp-badge dp-badge--busy',
  OFFLINE: 'dp-badge dp-badge--offline',
};

const STATUS_TEXT: Record<Driver['status'], string> = {
  AVAILABLE: 'Available',
  ON_DELIVERY: 'On delivery',
  OFFLINE: 'Offline',
};

export function AvailabilityToggle({ driver }: { driver: Driver }) {
  const queryClient = useQueryClient();

  const mutation = useMutation({
    mutationFn: (available: boolean) => setAvailability(driver.id, available),
    onSuccess: (updated) => {
      queryClient.setQueryData(['driver', driver.id], updated);
      void queryClient.invalidateQueries({ queryKey: ['deliveries'] });
    },
  });

  // A courier mid-delivery cannot go offline from here — the service rejects it.
  const lockedByDelivery = driver.status === 'ON_DELIVERY';
  const isAvailable = driver.status === 'AVAILABLE';

  return (
    <div className="dp-card">
      <div className="dp-row">
        <div>
          <h3>Shift status</h3>
          <span className={STATUS_BADGE[driver.status]}>
            {STATUS_TEXT[driver.status]}
          </span>
        </div>
        <label className="dp-toggle">
          <input
            type="checkbox"
            checked={isAvailable || driver.status === 'ON_DELIVERY'}
            disabled={mutation.isPending || lockedByDelivery}
            onChange={(e) => mutation.mutate(e.target.checked)}
          />
          <span className="dp-muted">
            {isAvailable ? 'Go offline' : 'Go online'}
          </span>
        </label>
      </div>

      {lockedByDelivery && (
        <p className="dp-muted">
          Finish your active delivery before going offline.
        </p>
      )}
      {mutation.isError && (
        <p className="dp-error">Could not update availability. Try again.</p>
      )}
    </div>
  );
}
