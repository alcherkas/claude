import { useMemo } from 'react';
import type { Delivery, EarningsSummary } from '../types';
import { formatCents, isActive, isTerminal } from '../delivery-flow';

// Earnings are derived from the courier's own deliveries: completed payouts sum
// into total earnings; in-flight deliveries are surfaced separately.
function summarize(deliveries: Delivery[]): EarningsSummary {
  let totalCents = 0;
  let completedCount = 0;
  let activeCount = 0;
  for (const d of deliveries) {
    if (d.status === 'DELIVERED') {
      totalCents += d.payoutCents;
      completedCount += 1;
    } else if (isActive(d.status)) {
      activeCount += 1;
    }
  }
  return { totalCents, completedCount, activeCount, currency: 'USD' };
}

export function EarningsPanel({ deliveries }: { deliveries: Delivery[] }) {
  const summary = useMemo(() => summarize(deliveries), [deliveries]);
  const recent = useMemo(
    () =>
      deliveries
        .filter((d) => isTerminal(d.status))
        .sort((a, b) => b.updatedAt.localeCompare(a.updatedAt))
        .slice(0, 5),
    [deliveries],
  );

  return (
    <div className="dp-card">
      <h3>Earnings today</h3>
      <div className="dp-earnings-grid">
        <div className="dp-stat">
          <div className="dp-muted">Earned</div>
          <div className="dp-stat-value">
            {formatCents(summary.totalCents, summary.currency)}
          </div>
        </div>
        <div className="dp-stat">
          <div className="dp-muted">Completed</div>
          <div className="dp-stat-value">{summary.completedCount}</div>
        </div>
        <div className="dp-stat">
          <div className="dp-muted">Active</div>
          <div className="dp-stat-value">{summary.activeCount}</div>
        </div>
      </div>

      {recent.length > 0 && (
        <>
          <h3 style={{ marginTop: 16 }}>Recent</h3>
          <ul className="dp-list">
            {recent.map((d) => (
              <li key={d.id} className="dp-row">
                <span className="dp-muted">order #{d.orderId}</span>
                <span>
                  {d.status === 'DELIVERED'
                    ? formatCents(d.payoutCents, summary.currency)
                    : 'Failed'}
                </span>
              </li>
            ))}
          </ul>
        </>
      )}
    </div>
  );
}
