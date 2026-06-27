import { useState } from 'react';
import { formatCents } from '../lib/money';

interface TipInputProps {
  // Tip presets are computed as a percentage of the cart subtotal.
  subtotalCents: number;
  // Current tip in cents (the authoritative value sent to pricing).
  tipCents: number;
  onChange: (tipCents: number) => void;
}

const PRESET_PERCENTS = [10, 15, 20] as const;

// Lets the customer add an optional courier tip — a preset percentage of the
// subtotal or a custom amount. The chosen cents are passed up so CartView can
// fold them into the pricing-quote request (pricing adds the tip to the total).
export function TipInput({ subtotalCents, tipCents, onChange }: TipInputProps) {
  const [custom, setCustom] = useState('');

  const presetCents = (percent: number) =>
    Math.round((subtotalCents * percent) / 100);

  return (
    <div className="tip">
      <span className="tip__label">Add a tip for your courier</span>
      <div className="tip__row">
        <button
          type="button"
          className={tipCents === 0 ? 'tip__option tip__option--active' : 'tip__option'}
          onClick={() => {
            setCustom('');
            onChange(0);
          }}
        >
          No tip
        </button>
        {PRESET_PERCENTS.map((percent) => {
          const cents = presetCents(percent);
          return (
            <button
              key={percent}
              type="button"
              className={
                tipCents === cents && cents > 0
                  ? 'tip__option tip__option--active'
                  : 'tip__option'
              }
              onClick={() => {
                setCustom('');
                onChange(cents);
              }}
            >
              {percent}% ({formatCents(cents)})
            </button>
          );
        })}
      </div>
      <label htmlFor="tip-custom">Custom tip</label>
      <input
        id="tip-custom"
        type="number"
        min="0"
        step="0.01"
        inputMode="decimal"
        placeholder="0.00"
        value={custom}
        onChange={(e) => {
          const raw = e.target.value;
          setCustom(raw);
          const dollars = Number.parseFloat(raw);
          onChange(Number.isFinite(dollars) && dollars > 0 ? Math.round(dollars * 100) : 0);
        }}
      />
    </div>
  );
}
