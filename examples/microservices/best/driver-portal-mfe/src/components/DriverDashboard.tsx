import { useMemo } from 'react';
import { useQuery } from '@tanstack/react-query';
import { getDriver, listDeliveries } from '../api/client';
import { getCurrentUser } from '../session';
import { isActive } from '../delivery-flow';
import { AvailabilityToggle } from './AvailabilityToggle';
import { AvailableDeliveries } from './AvailableDeliveries';
import { ActiveDelivery } from './ActiveDelivery';
import { EarningsPanel } from './EarningsPanel';

// In QuickBite the driver id equals the courier's user id (driver-service keys
// the Driver record by userId). The shell host persists the signed-in user, so
// the portal resolves its driver from that.
export function DriverDashboard() {
  const user = getCurrentUser();
  const driverId = user?.id ?? null;

  const driverQuery = useQuery({
    queryKey: ['driver', driverId],
    queryFn: () => getDriver(driverId as string),
    enabled: Boolean(driverId),
  });

  const deliveriesQuery = useQuery({
    queryKey: ['deliveries', 'assigned', driverId],
    queryFn: () => listDeliveries(driverId as string),
    enabled: Boolean(driverId),
    refetchInterval: 10_000,
  });

  const deliveries = useMemo(
    () => deliveriesQuery.data ?? [],
    [deliveriesQuery.data],
  );
  const activeDelivery = useMemo(
    () => deliveries.find((d) => isActive(d.status)) ?? null,
    [deliveries],
  );

  if (!driverId) {
    return (
      <div className="dp-card dp-muted">
        Sign in as a courier to open the driver portal.
      </div>
    );
  }

  if (driverQuery.isLoading) {
    return <div className="dp-card dp-muted">Loading your shift…</div>;
  }

  if (driverQuery.isError || !driverQuery.data) {
    return (
      <div className="dp-card dp-error">
        Could not load your driver profile. Please retry.
      </div>
    );
  }

  const driver = driverQuery.data;

  return (
    <div>
      <div className="dp-row">
        <h1>Driver portal</h1>
        <span className="dp-muted">{driver.name}</span>
      </div>

      <AvailabilityToggle driver={driver} />

      {activeDelivery ? (
        <ActiveDelivery delivery={activeDelivery} driverId={driverId} />
      ) : (
        driver.status === 'AVAILABLE' && (
          <AvailableDeliveries driverId={driverId} />
        )
      )}

      <EarningsPanel deliveries={deliveries} />
    </div>
  );
}
