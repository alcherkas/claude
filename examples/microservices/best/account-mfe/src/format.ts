// Formatting helpers shared by the account MFE components.

// Renders integer minor units (cents) as a localized currency string.
export function formatMoney(amountCents: number, currency = 'USD'): string {
  return new Intl.NumberFormat(undefined, {
    style: 'currency',
    currency,
  }).format(amountCents / 100);
}

// Human-friendly date/time for order and review timestamps.
export function formatDateTime(iso: string): string {
  return new Date(iso).toLocaleString();
}
