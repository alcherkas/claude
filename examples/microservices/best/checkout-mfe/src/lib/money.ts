// Cents -> human readable money. The platform stores all monetary values as
// integer cents (PLATFORM_SPEC §3); presentation happens only at the edge.
export function formatCents(cents: number, currency = 'USD'): string {
  return new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency,
  }).format(cents / 100);
}
