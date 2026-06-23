import { useState } from 'react';
import type { MenuItem } from '../types';

interface MenuItemCardProps {
  item: MenuItem;
  onAdd: (item: MenuItem, qty: number) => void;
}

function formatPrice(priceCents: number, currency: string): string {
  try {
    return new Intl.NumberFormat(undefined, {
      style: 'currency',
      currency,
    }).format(priceCents / 100);
  } catch {
    return `${(priceCents / 100).toFixed(2)} ${currency}`;
  }
}

// A single menu line with a quantity stepper and an "Add" action. The add is
// handed off to checkout-mfe via a cross-MFE event (MenuView), since
// discovery-mfe does not call cart-service directly (PLATFORM_SPEC §1.2).
export function MenuItemCard({ item, onAdd }: MenuItemCardProps) {
  const [qty, setQty] = useState(1);

  return (
    <li
      className={`dc-menuitem${item.available ? '' : ' dc-menuitem--unavailable'}`}
    >
      <div className="dc-menuitem__info">
        <h4 className="dc-menuitem__name">{item.name}</h4>
        <p className="dc-menuitem__desc">{item.description}</p>
        <span className="dc-menuitem__category">{item.category}</span>
      </div>

      <div className="dc-menuitem__actions">
        <span className="dc-menuitem__price">
          {formatPrice(item.priceCents, item.currency)}
        </span>

        {item.available ? (
          <div className="dc-qty">
            <button
              type="button"
              className="dc-qty__btn"
              aria-label="Decrease quantity"
              onClick={() => setQty((n) => Math.max(1, n - 1))}
            >
              −
            </button>
            <span className="dc-qty__value">{qty}</span>
            <button
              type="button"
              className="dc-qty__btn"
              aria-label="Increase quantity"
              onClick={() => setQty((n) => Math.min(20, n + 1))}
            >
              +
            </button>
            <button
              type="button"
              className="qb-btn dc-menuitem__add"
              onClick={() => onAdd(item, qty)}
            >
              Add
            </button>
          </div>
        ) : (
          <span className="dc-menuitem__sold-out">Unavailable</span>
        )}
      </div>
    </li>
  );
}
