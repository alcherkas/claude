import type { Quote } from '../types';
import { formatCents } from '../lib/money';

interface OrderSummaryProps {
  quote?: Quote;
  loading?: boolean;
  title?: string;
}

// Renders a pricing-service quote (subtotal, fees, tax, discount, total). The
// quote is authoritative — these numbers are exactly what order/payment use.
export function OrderSummary({ quote, loading, title }: OrderSummaryProps) {
  return (
    <aside className="summary">
      <h3>{title ?? 'Order summary'}</h3>

      {loading && <p className="summary__muted">Pricing your order…</p>}

      {!loading && !quote && (
        <p className="summary__muted">Add items to see your total.</p>
      )}

      {quote && (
        <>
          <dl className="summary__lines">
            <Row label="Subtotal" cents={quote.subtotalCents} />
            <Row label="Delivery fee" cents={quote.deliveryFeeCents} />
            <Row label="Service fee" cents={quote.serviceFeeCents} />
            <Row label="Tax" cents={quote.taxCents} />
            {quote.discountCents > 0 && (
              <Row
                label="Discount"
                cents={-quote.discountCents}
                emphasis="discount"
              />
            )}
            {quote.tipCents > 0 && (
              <Row label="Courier tip" cents={quote.tipCents} />
            )}
          </dl>
          <div className="summary__total">
            <span>Total</span>
            <strong>{formatCents(quote.totalCents)}</strong>
          </div>
        </>
      )}
    </aside>
  );
}

function Row({
  label,
  cents,
  emphasis,
}: {
  label: string;
  cents: number;
  emphasis?: 'discount';
}) {
  return (
    <div className={emphasis ? `summary__line summary__line--${emphasis}` : 'summary__line'}>
      <dt>{label}</dt>
      <dd>{formatCents(cents)}</dd>
    </div>
  );
}
