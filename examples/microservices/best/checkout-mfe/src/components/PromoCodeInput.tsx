import { useState } from 'react';
import { useMutation } from '@tanstack/react-query';
import { validatePromo } from '../api/client';
import type { PromoValidation } from '../types';

interface PromoCodeInputProps {
  subtotalCents: number;
  appliedCode: string | null;
  onApply: (validation: PromoValidation) => void;
  onClear: () => void;
}

// Validates a promo code against promotion-service before it is carried into
// the pricing quote. The authoritative discount is still computed server-side
// by pricing-service; this is a fast pre-check + UX affordance.
export function PromoCodeInput({
  subtotalCents,
  appliedCode,
  onApply,
  onClear,
}: PromoCodeInputProps) {
  const [code, setCode] = useState('');

  const validation = useMutation({
    mutationFn: (raw: string) => validatePromo(raw.trim(), subtotalCents),
    onSuccess: (result) => {
      if (result.valid) {
        onApply(result);
      }
    },
  });

  if (appliedCode) {
    return (
      <div className="promo promo--applied">
        <span>
          Promo <strong>{appliedCode}</strong> applied
        </span>
        <button
          type="button"
          className="link-danger"
          onClick={() => {
            validation.reset();
            setCode('');
            onClear();
          }}
        >
          Remove
        </button>
      </div>
    );
  }

  const rejected =
    validation.isSuccess && validation.data && !validation.data.valid;

  return (
    <form
      className="promo"
      onSubmit={(e) => {
        e.preventDefault();
        if (code.trim()) {
          validation.mutate(code);
        }
      }}
    >
      <label htmlFor="promo-code">Promo code</label>
      <div className="promo__row">
        <input
          id="promo-code"
          type="text"
          autoComplete="off"
          placeholder="e.g. WELCOME10"
          value={code}
          onChange={(e) => setCode(e.target.value.toUpperCase())}
        />
        <button type="submit" disabled={!code.trim() || validation.isPending}>
          {validation.isPending ? 'Checking…' : 'Apply'}
        </button>
      </div>
      {rejected && (
        <p className="promo__error" role="alert">
          {validation.data?.message ?? 'That code is not valid.'}
        </p>
      )}
      {validation.isError && (
        <p className="promo__error" role="alert">
          Could not validate the code. Please try again.
        </p>
      )}
    </form>
  );
}
