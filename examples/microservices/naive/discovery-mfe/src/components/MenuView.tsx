import { useMemo, useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { getMenu } from '../api/client';
import { emitAddToCart } from '../session';
import type { MenuItem } from '../types';
import { MenuItemCard } from './MenuItemCard';

interface MenuViewProps {
  restaurantId: string;
}

type Notice = { kind: 'success'; message: string } | null;

// Fetches a restaurant's menu (GET /api/menu?restaurantId=) and lets the user
// add items toward their cart. discovery-mfe does not own the cart
// (PLATFORM_SPEC §1.2): "Add" emits a cross-MFE event that the shell host
// forwards to checkout-mfe. Items are grouped by category for display.
export function MenuView({ restaurantId }: MenuViewProps) {
  const [notice, setNotice] = useState<Notice>(null);

  const menuQuery = useQuery({
    queryKey: ['menu', restaurantId],
    queryFn: () => getMenu(restaurantId),
    enabled: Boolean(restaurantId),
  });

  const grouped = useMemo(() => {
    const items = menuQuery.data ?? [];
    const byCategory = new Map<string, MenuItem[]>();
    for (const item of items) {
      const list = byCategory.get(item.category) ?? [];
      list.push(item);
      byCategory.set(item.category, list);
    }
    return Array.from(byCategory.entries());
  }, [menuQuery.data]);

  const handleAdd = (item: MenuItem, qty: number) => {
    emitAddToCart({
      restaurantId: item.restaurantId,
      menuItemId: item.id,
      qty,
      unitPriceCents: item.priceCents,
      name: item.name,
    });
    setNotice({
      kind: 'success',
      message: `Added ${qty} × ${item.name} to your cart.`,
    });
  };

  if (menuQuery.isLoading) {
    return <div className="dc-status">Loading menu…</div>;
  }
  if (menuQuery.isError) {
    return (
      <div className="dc-status dc-status--error">
        Could not load this menu.
      </div>
    );
  }
  if (grouped.length === 0) {
    return <div className="dc-status">This restaurant has no menu yet.</div>;
  }

  return (
    <section className="dc-menu">
      {notice && (
        <p className={`dc-notice dc-notice--${notice.kind}`} role="status">
          {notice.message}
        </p>
      )}

      {grouped.map(([category, items]) => (
        <div key={category} className="dc-menu__section">
          <h3 className="dc-menu__category">{category}</h3>
          <ul className="dc-menu__items">
            {items.map((item) => (
              <MenuItemCard key={item.id} item={item} onAdd={handleAdd} />
            ))}
          </ul>
        </div>
      ))}
    </section>
  );
}
