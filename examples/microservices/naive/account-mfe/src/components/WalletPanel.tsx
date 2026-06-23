import { useState } from 'react';
import {
  useMutation,
  useQuery,
  useQueryClient,
} from '@tanstack/react-query';
import { addCredit, getMe, getWallet } from '../api/client';
import type { AddCreditRequest } from '../types';
import { formatMoney } from '../format';

// Wallet balance + top-up — wallet-service GET /api/wallets/{userId} and
// POST /api/wallets/{userId}/credits. The userId is resolved from the current
// identity (GET /api/users/me).
export function WalletPanel() {
  const queryClient = useQueryClient();
  const [amount, setAmount] = useState('10.00');
  const [formError, setFormError] = useState<string | null>(null);

  const { data: user } = useQuery({ queryKey: ['me'], queryFn: getMe });
  const userId = user?.id;

  const {
    data: wallet,
    isLoading,
    isError,
  } = useQuery({
    queryKey: ['wallet', userId],
    queryFn: () => getWallet(userId as string),
    enabled: Boolean(userId),
  });

  const creditMutation = useMutation({
    mutationFn: (body: AddCreditRequest) =>
      addCredit(userId as string, body),
    onSuccess: (updated) => {
      queryClient.setQueryData(['wallet', userId], updated);
    },
  });

  function handleTopUp(e: React.FormEvent) {
    e.preventDefault();
    setFormError(null);
    const parsed = Number(amount);
    if (!Number.isFinite(parsed) || parsed <= 0) {
      setFormError('Enter an amount greater than zero.');
      return;
    }
    creditMutation.mutate({
      amountCents: Math.round(parsed * 100),
      currency: wallet?.currency ?? 'USD',
      description: 'Customer top-up',
    });
  }

  if (isLoading || !userId) {
    return <div className="acct-card acct-muted">Loading wallet…</div>;
  }

  if (isError || !wallet) {
    return (
      <div className="acct-card acct-error">Could not load your wallet.</div>
    );
  }

  return (
    <section className="acct-card acct-wallet">
      <div className="acct-wallet__balance">
        <span className="acct-wallet__label">Available balance</span>
        <span className="acct-wallet__amount">
          {formatMoney(wallet.balanceCents, wallet.currency)}
        </span>
      </div>

      <form className="acct-wallet__form" onSubmit={handleTopUp}>
        <label className="acct-field">
          Add credit ({wallet.currency})
          <input
            type="number"
            min="0.01"
            step="0.01"
            value={amount}
            onChange={(e) => setAmount(e.target.value)}
          />
        </label>
        <button
          type="submit"
          className="acct-btn"
          disabled={creditMutation.isPending}
        >
          {creditMutation.isPending ? 'Adding…' : 'Add credit'}
        </button>
      </form>

      {formError && <p className="acct-form-error">{formError}</p>}
      {creditMutation.isError && (
        <p className="acct-form-error">
          Top-up failed. Please try again.
        </p>
      )}
      {creditMutation.isSuccess && !creditMutation.isPending && (
        <p className="acct-form-ok">Credit added to your wallet.</p>
      )}
    </section>
  );
}
