import type { DeliveryStatus } from './types';

// The forward courier workflow for a delivery (PLATFORM_SPEC §3 delivery-service
// status machine). Each status maps to the single next status a driver advances
// to, plus a human label for the action button.
const NEXT: Partial<Record<DeliveryStatus, DeliveryStatus>> = {
  ASSIGNED: 'EN_ROUTE_TO_PICKUP',
  EN_ROUTE_TO_PICKUP: 'PICKED_UP',
  PICKED_UP: 'EN_ROUTE_TO_CUSTOMER',
  EN_ROUTE_TO_CUSTOMER: 'DELIVERED',
};

const ACTION_LABELS: Partial<Record<DeliveryStatus, string>> = {
  ASSIGNED: 'Head to pickup',
  EN_ROUTE_TO_PICKUP: 'Mark picked up',
  PICKED_UP: 'Start drop-off',
  EN_ROUTE_TO_CUSTOMER: 'Mark delivered',
};

export function nextStatus(status: DeliveryStatus): DeliveryStatus | null {
  return NEXT[status] ?? null;
}

export function nextActionLabel(status: DeliveryStatus): string | null {
  return ACTION_LABELS[status] ?? null;
}

// A delivery is "active" for a courier when it is assigned but not yet in a
// terminal state.
const TERMINAL: DeliveryStatus[] = ['DELIVERED', 'FAILED'];

export function isActive(status: DeliveryStatus): boolean {
  return status !== 'PENDING' && !TERMINAL.includes(status);
}

export function isTerminal(status: DeliveryStatus): boolean {
  return TERMINAL.includes(status);
}

export function statusLabel(status: DeliveryStatus): string {
  return status
    .toLowerCase()
    .split('_')
    .map((w) => w.charAt(0).toUpperCase() + w.slice(1))
    .join(' ');
}

export function formatCents(cents: number, currency = 'USD'): string {
  return new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency,
  }).format(cents / 100);
}
