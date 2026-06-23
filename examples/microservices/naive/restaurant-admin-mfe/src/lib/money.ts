// Format integer minor units (cents) as a localized currency string.
// All money on the QuickBite platform is carried as integer *Cents fields
// (PLATFORM_SPEC §3) to avoid floating-point drift.
export function formatMoney(cents: number, currency = 'USD'): string {
  return new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency,
  }).format(cents / 100);
}

// Parse a user-entered major-unit amount (e.g. "12.50") into integer cents.
// Returns null when the input is not a valid non-negative number.
export function parseMoneyToCents(input: string): number | null {
  const trimmed = input.trim();
  if (trimmed === '') return null;
  const value = Number(trimmed);
  if (!Number.isFinite(value) || value < 0) return null;
  return Math.round(value * 100);
}
