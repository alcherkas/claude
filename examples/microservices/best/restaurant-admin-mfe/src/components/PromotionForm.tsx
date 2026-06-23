import { type FormEvent, useState } from 'react';
import type { CreatePromotionRequest, PromotionType } from '../types';
import { parseMoneyToCents } from '../lib/money';

interface PromotionFormProps {
  restaurantId: string;
  onSubmit: (body: CreatePromotionRequest) => void;
  submitting: boolean;
  error: boolean;
}

interface FormState {
  code: string;
  type: PromotionType;
  // PERCENT: whole percent; FIXED: major-unit amount; FREE_DELIVERY: unused.
  value: string;
  minSubtotal: string;
  validFrom: string;
  validTo: string;
  maxRedemptions: string;
  perUserLimit: string;
  active: boolean;
}

const EMPTY: FormState = {
  code: '',
  type: 'PERCENT',
  value: '',
  minSubtotal: '',
  validFrom: '',
  validTo: '',
  maxRedemptions: '100',
  perUserLimit: '1',
  active: true,
};

// Resolve the form's `value` field into the integer the promotion-service
// expects: a percentage for PERCENT, cents for FIXED, 0 for FREE_DELIVERY.
function resolveValue(type: PromotionType, raw: string): number | null {
  if (type === 'FREE_DELIVERY') return 0;
  if (type === 'PERCENT') {
    const pct = Number(raw.trim());
    if (!Number.isInteger(pct) || pct <= 0 || pct > 100) return null;
    return pct;
  }
  return parseMoneyToCents(raw);
}

export function PromotionForm({
  restaurantId,
  onSubmit,
  submitting,
  error,
}: PromotionFormProps) {
  const [form, setForm] = useState<FormState>(EMPTY);

  const resolvedValue = resolveValue(form.type, form.value);
  const minSubtotalCents = parseMoneyToCents(form.minSubtotal) ?? 0;
  const maxRedemptions = Number(form.maxRedemptions);
  const perUserLimit = Number(form.perUserLimit);

  const valid =
    form.code.trim() !== '' &&
    resolvedValue !== null &&
    form.validFrom !== '' &&
    form.validTo !== '' &&
    form.validFrom <= form.validTo &&
    Number.isInteger(maxRedemptions) &&
    maxRedemptions > 0 &&
    Number.isInteger(perUserLimit) &&
    perUserLimit > 0;

  function handleSubmit(e: FormEvent) {
    e.preventDefault();
    if (!valid || resolvedValue === null) return;
    onSubmit({
      code: form.code.trim().toUpperCase(),
      type: form.type,
      value: resolvedValue,
      minSubtotalCents,
      // Send full-day ISO instants derived from the date inputs.
      validFrom: new Date(`${form.validFrom}T00:00:00Z`).toISOString(),
      validTo: new Date(`${form.validTo}T23:59:59Z`).toISOString(),
      maxRedemptions,
      perUserLimit,
      active: form.active,
      restaurantId,
    });
    setForm(EMPTY);
  }

  return (
    <form className="ra-card" onSubmit={handleSubmit}>
      <h3>New promotion</h3>
      <div className="ra-row">
        <div className="ra-field" style={{ flex: 1 }}>
          <label htmlFor="p-code">Code</label>
          <input
            id="p-code"
            placeholder="WELCOME10"
            value={form.code}
            onChange={(e) => setForm({ ...form, code: e.target.value })}
          />
        </div>
        <div className="ra-field" style={{ width: 160 }}>
          <label htmlFor="p-type">Type</label>
          <select
            id="p-type"
            value={form.type}
            onChange={(e) =>
              setForm({ ...form, type: e.target.value as PromotionType })
            }
          >
            <option value="PERCENT">Percent off</option>
            <option value="FIXED">Fixed amount off</option>
            <option value="FREE_DELIVERY">Free delivery</option>
          </select>
        </div>
      </div>

      {form.type !== 'FREE_DELIVERY' && (
        <div className="ra-field">
          <label htmlFor="p-value">
            {form.type === 'PERCENT' ? 'Percent (1–100)' : 'Amount off'}
          </label>
          <input
            id="p-value"
            inputMode="decimal"
            placeholder={form.type === 'PERCENT' ? '10' : '5.00'}
            value={form.value}
            onChange={(e) => setForm({ ...form, value: e.target.value })}
          />
          {form.value !== '' && resolvedValue === null && (
            <span className="ra-error">Enter a valid value.</span>
          )}
        </div>
      )}

      <div className="ra-field">
        <label htmlFor="p-min">Minimum subtotal</label>
        <input
          id="p-min"
          inputMode="decimal"
          placeholder="0.00"
          value={form.minSubtotal}
          onChange={(e) => setForm({ ...form, minSubtotal: e.target.value })}
        />
      </div>

      <div className="ra-row">
        <div className="ra-field" style={{ flex: 1 }}>
          <label htmlFor="p-from">Valid from</label>
          <input
            id="p-from"
            type="date"
            value={form.validFrom}
            onChange={(e) => setForm({ ...form, validFrom: e.target.value })}
          />
        </div>
        <div className="ra-field" style={{ flex: 1 }}>
          <label htmlFor="p-to">Valid to</label>
          <input
            id="p-to"
            type="date"
            value={form.validTo}
            onChange={(e) => setForm({ ...form, validTo: e.target.value })}
          />
        </div>
      </div>

      <div className="ra-row">
        <div className="ra-field" style={{ flex: 1 }}>
          <label htmlFor="p-max">Max redemptions</label>
          <input
            id="p-max"
            type="number"
            min={1}
            value={form.maxRedemptions}
            onChange={(e) =>
              setForm({ ...form, maxRedemptions: e.target.value })
            }
          />
        </div>
        <div className="ra-field" style={{ flex: 1 }}>
          <label htmlFor="p-peruser">Per-user limit</label>
          <input
            id="p-peruser"
            type="number"
            min={1}
            value={form.perUserLimit}
            onChange={(e) => setForm({ ...form, perUserLimit: e.target.value })}
          />
        </div>
      </div>

      <div className="ra-field">
        <label htmlFor="p-active">
          <input
            id="p-active"
            type="checkbox"
            checked={form.active}
            onChange={(e) => setForm({ ...form, active: e.target.checked })}
          />{' '}
          Active
        </label>
      </div>

      {error && <p className="ra-error">Could not create the promotion.</p>}
      <div className="ra-form-actions">
        <button type="submit" className="ra-btn" disabled={!valid || submitting}>
          {submitting ? 'Creating…' : 'Create promotion'}
        </button>
      </div>
    </form>
  );
}
