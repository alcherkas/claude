import { useState } from 'react';

interface TipSelectorProps {
  // Tips are presets as a percentage of the food subtotal (matching the
  // server-side basis), plus a custom dollar amount.
  subtotalCents: number;
  tipCents: number;
  onChange: (tipCents: number) => void;
}

const PRESETS = [10, 15, 20] as const;

// Round a percentage of the subtotal to whole cents.
function presetCents(subtotalCents: number, percent: number): number {
  return Math.round((subtotalCents * percent) / 100);
}

export function TipSelector({
  subtotalCents,
  tipCents,
  onChange,
}: TipSelectorProps) {
  const [customMode, setCustomMode] = useState(false);
  const [customDollars, setCustomDollars] = useState('');

  const matchedPreset = PRESETS.find(
    (p) => !customMode && presetCents(subtotalCents, p) === tipCents,
  );
  const noTipSelected = !customMode && tipCents === 0;

  function selectPreset(percent: number) {
    setCustomMode(false);
    setCustomDollars('');
    onChange(presetCents(subtotalCents, percent));
  }

  function selectNoTip() {
    setCustomMode(false);
    setCustomDollars('');
    onChange(0);
  }

  function applyCustom(value: string) {
    setCustomDollars(value);
    const dollars = Number.parseFloat(value);
    onChange(Number.isFinite(dollars) && dollars > 0 ? Math.round(dollars * 100) : 0);
  }

  return (
    <div className="tip">
      <span className="tip__label">Add a tip for your courier</span>
      <div className="tip__options">
        <button
          type="button"
          className={noTipSelected ? 'tip__option tip__option--active' : 'tip__option'}
          onClick={selectNoTip}
        >
          No tip
        </button>
        {PRESETS.map((percent) => (
          <button
            key={percent}
            type="button"
            className={
              matchedPreset === percent
                ? 'tip__option tip__option--active'
                : 'tip__option'
            }
            onClick={() => selectPreset(percent)}
          >
            {percent}%
          </button>
        ))}
        <button
          type="button"
          className={customMode ? 'tip__option tip__option--active' : 'tip__option'}
          onClick={() => {
            setCustomMode(true);
            applyCustom(customDollars);
          }}
        >
          Custom
        </button>
      </div>
      {customMode && (
        <div className="tip__custom">
          <label htmlFor="tip-custom">Tip amount ($)</label>
          <input
            id="tip-custom"
            type="number"
            min="0"
            step="0.01"
            inputMode="decimal"
            placeholder="0.00"
            value={customDollars}
            onChange={(e) => applyCustom(e.target.value)}
          />
        </div>
      )}
    </div>
  );
}
