import type { CartItem } from '../types';
import { formatCents } from '../lib/money';

interface CartItemRowProps {
  item: CartItem;
  busy?: boolean;
  onChangeQty: (menuItemId: string, qty: number) => void;
  onRemove: (menuItemId: string) => void;
}

// A single editable line in the cart. Quantity changes and removal are pushed
// back to cart-service by the parent CartView.
export function CartItemRow({
  item,
  busy,
  onChangeQty,
  onRemove,
}: CartItemRowProps) {
  const lineTotal = item.unitPriceCents * item.qty;

  return (
    <tr className="cart-row">
      <td className="cart-row__name">
        <span>{item.name}</span>
        <small>{formatCents(item.unitPriceCents)} each</small>
      </td>
      <td className="cart-row__qty">
        <button
          type="button"
          aria-label={`Decrease ${item.name}`}
          disabled={busy || item.qty <= 1}
          onClick={() => onChangeQty(item.menuItemId, item.qty - 1)}
        >
          −
        </button>
        <span>{item.qty}</span>
        <button
          type="button"
          aria-label={`Increase ${item.name}`}
          disabled={busy}
          onClick={() => onChangeQty(item.menuItemId, item.qty + 1)}
        >
          +
        </button>
      </td>
      <td className="cart-row__total">{formatCents(lineTotal)}</td>
      <td className="cart-row__actions">
        <button
          type="button"
          className="link-danger"
          disabled={busy}
          onClick={() => onRemove(item.menuItemId)}
        >
          Remove
        </button>
      </td>
    </tr>
  );
}
