import {
  useMutation,
  useQuery,
  useQueryClient,
} from '@tanstack/react-query';
import { advanceOrder, restaurantOrders } from '../api/client';
import type { Order, OrderStatus } from '../types';
import { useSelectedRestaurant } from '../lib/selectedRestaurant';
import { formatMoney } from '../lib/money';

const STATUS_BADGE: Record<OrderStatus, string> = {
  CREATED: 'ra-badge--warn',
  CONFIRMED: 'ra-badge--warn',
  PREPARING: 'ra-badge--warn',
  READY: 'ra-badge--ok',
  PICKED_UP: 'ra-badge--muted',
  DELIVERED: 'ra-badge--muted',
  CANCELLED: 'ra-badge--danger',
};

// The kitchen-side slice of the order lifecycle (PLATFORM_SPEC §3). Past READY
// the order leaves the restaurant's hands (courier pickup / delivery), so the
// owner can only advance up to READY here. CANCELLED is a terminal escape hatch
// from any pre-pickup state.
const NEXT_STATUS: Partial<Record<OrderStatus, OrderStatus>> = {
  CREATED: 'CONFIRMED',
  CONFIRMED: 'PREPARING',
  PREPARING: 'READY',
};

const CANCELLABLE: ReadonlySet<OrderStatus> = new Set<OrderStatus>([
  'CREATED',
  'CONFIRMED',
  'PREPARING',
]);

export function IncomingOrders() {
  const queryClient = useQueryClient();
  const { restaurantId } = useSelectedRestaurant();

  const ordersQuery = useQuery({
    queryKey: ['orders', restaurantId],
    queryFn: () => restaurantOrders({ restaurantId: restaurantId as string }),
    enabled: Boolean(restaurantId),
    // Incoming orders are time-sensitive — poll while the tab is open.
    refetchInterval: 10_000,
  });

  const advanceMutation = useMutation({
    mutationFn: ({ id, status }: { id: string; status: OrderStatus }) =>
      advanceOrder(id, { status }),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['orders', restaurantId] });
    },
  });

  if (!restaurantId) {
    return (
      <p className="ra-muted">
        Select a restaurant on the Dashboard to see incoming orders.
      </p>
    );
  }
  if (ordersQuery.isLoading) {
    return <p className="ra-muted">Loading orders…</p>;
  }
  if (ordersQuery.isError) {
    return <p className="ra-error">Could not load orders.</p>;
  }

  const orders = ordersQuery.data ?? [];
  const active = orders.filter(
    (o) => o.status !== 'DELIVERED' && o.status !== 'CANCELLED',
  );

  return (
    <section>
      <div className="ra-row" style={{ marginBottom: '1rem' }}>
        <h2>Incoming orders</h2>
        {ordersQuery.isFetching && <span className="ra-muted">refreshing…</span>}
      </div>

      {active.length === 0 ? (
        <p className="ra-muted">No active orders right now.</p>
      ) : (
        active.map((order) => (
          <OrderCard
            key={order.id}
            order={order}
            onAdvance={() => {
              const next = NEXT_STATUS[order.status];
              if (next) advanceMutation.mutate({ id: order.id, status: next });
            }}
            onCancel={() =>
              advanceMutation.mutate({ id: order.id, status: 'CANCELLED' })
            }
            busy={
              advanceMutation.isPending &&
              advanceMutation.variables?.id === order.id
            }
          />
        ))
      )}
    </section>
  );
}

function OrderCard({
  order,
  onAdvance,
  onCancel,
  busy,
}: {
  order: Order;
  onAdvance: () => void;
  onCancel: () => void;
  busy: boolean;
}) {
  const next = NEXT_STATUS[order.status];
  const cancellable = CANCELLABLE.has(order.status);
  // Pricing snapshots are denominated in the platform's settlement currency.
  const currency = 'USD';

  return (
    <article className="ra-card">
      <div className="ra-row">
        <h3>Order #{order.id.slice(0, 8)}</h3>
        <span className={`ra-badge ${STATUS_BADGE[order.status]}`}>
          {order.status}
        </span>
      </div>
      <p className="ra-muted">
        Placed {new Date(order.createdAt).toLocaleString()}
      </p>
      <ul>
        {order.items.map((it) => (
          <li key={it.menuItemId}>
            {it.qty} × {it.name} —{' '}
            {formatMoney(it.unitPriceCents * it.qty, currency)}
          </li>
        ))}
      </ul>
      <div className="ra-row">
        <strong>Total {formatMoney(order.pricing.totalCents, currency)}</strong>
        <div className="ra-form-actions">
          {cancellable && (
            <button
              type="button"
              className="ra-btn ra-btn--ghost"
              onClick={onCancel}
              disabled={busy}
            >
              Cancel
            </button>
          )}
          {next && (
            <button
              type="button"
              className="ra-btn"
              onClick={onAdvance}
              disabled={busy}
            >
              Mark {next}
            </button>
          )}
        </div>
      </div>
    </article>
  );
}
