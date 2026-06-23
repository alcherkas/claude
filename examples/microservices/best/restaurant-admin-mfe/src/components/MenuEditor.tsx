import {
  useMutation,
  useQuery,
  useQueryClient,
} from '@tanstack/react-query';
import {
  createMenuItem,
  listMenu,
  toggleAvailability,
} from '../api/client';
import type { CreateMenuItemRequest, MenuItem } from '../types';
import { useSelectedRestaurant } from '../lib/selectedRestaurant';
import { formatMoney } from '../lib/money';
import { MenuItemForm } from './MenuItemForm';

export function MenuEditor() {
  const queryClient = useQueryClient();
  const { restaurantId } = useSelectedRestaurant();

  const menuQuery = useQuery({
    queryKey: ['menu', restaurantId],
    queryFn: () => listMenu(restaurantId as string),
    enabled: Boolean(restaurantId),
  });

  const createMutation = useMutation({
    mutationFn: (body: CreateMenuItemRequest) => createMenuItem(body),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['menu', restaurantId] });
    },
  });

  const availabilityMutation = useMutation({
    mutationFn: ({ id, available }: { id: string; available: boolean }) =>
      toggleAvailability(id, available),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['menu', restaurantId] });
    },
  });

  if (!restaurantId) {
    return (
      <p className="ra-muted">
        Select a restaurant on the Dashboard to edit its menu.
      </p>
    );
  }
  if (menuQuery.isLoading) {
    return <p className="ra-muted">Loading menu…</p>;
  }
  if (menuQuery.isError) {
    return <p className="ra-error">Could not load the menu.</p>;
  }

  const items = menuQuery.data ?? [];

  return (
    <section>
      <h2>Menu</h2>

      <MenuItemForm
        restaurantId={restaurantId}
        onSubmit={(body) => createMutation.mutate(body)}
        submitting={createMutation.isPending}
        error={createMutation.isError}
      />

      {items.length === 0 ? (
        <p className="ra-muted">No menu items yet. Add your first above.</p>
      ) : (
        <div className="ra-grid">
          {items.map((item) => (
            <MenuItemCard
              key={item.id}
              item={item}
              onToggle={() =>
                availabilityMutation.mutate({
                  id: item.id,
                  available: !item.available,
                })
              }
              toggling={
                availabilityMutation.isPending &&
                availabilityMutation.variables?.id === item.id
              }
            />
          ))}
        </div>
      )}
    </section>
  );
}

function MenuItemCard({
  item,
  onToggle,
  toggling,
}: {
  item: MenuItem;
  onToggle: () => void;
  toggling: boolean;
}) {
  return (
    <article className="ra-card">
      <div className="ra-row">
        <h3>{item.name}</h3>
        <span
          className={`ra-badge ${
            item.available ? 'ra-badge--ok' : 'ra-badge--muted'
          }`}
        >
          {item.available ? 'Available' : 'Hidden'}
        </span>
      </div>
      <p className="ra-muted">{item.category}</p>
      {item.description && <p>{item.description}</p>}
      <div className="ra-row">
        <strong>{formatMoney(item.priceCents, item.currency)}</strong>
        <button
          type="button"
          className="ra-btn ra-btn--ghost"
          onClick={onToggle}
          disabled={toggling}
        >
          {item.available ? 'Hide' : 'Make available'}
        </button>
      </div>
    </article>
  );
}
