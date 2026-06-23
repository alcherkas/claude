import { useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { getWallet, pay } from '../api/client';
import type {
  CardDetails,
  Order,
  Payment,
  PaymentMethod,
} from '../types';
import { currentUserId } from '../lib/session';
import { formatCents } from '../lib/money';

// Step 3 of the flow: pay for the created order. Supports CARD (mock PSP) or
// WALLET (wallet-service debit). On success the order is captured and we show
// the confirmation.
export function PaymentForm() {
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const { orderId } = useParams<{ orderId: string }>();
  const userId = currentUserId();

  const [method, setMethod] = useState<PaymentMethod>('CARD');
  const [card, setCard] = useState<CardDetails>({
    number: '',
    expiry: '',
    cvc: '',
    holder: '',
  });

  const order = queryClient.getQueryData<Order>(['order', orderId ?? '']);
  const amountCents = order?.pricing.totalCents ?? 0;

  const walletQuery = useQuery({
    queryKey: ['wallet', userId],
    queryFn: () => getWallet(userId as string),
    enabled: Boolean(userId),
  });
  const wallet = walletQuery.data;
  const walletInsufficient =
    Boolean(wallet) && wallet!.balanceCents < amountCents;

  const payMutation = useMutation({
    mutationFn: () =>
      pay({
        orderId: orderId as string,
        userId: userId as string,
        amountCents,
        method,
        card: method === 'CARD' ? card : undefined,
      }),
    onSuccess: (payment: Payment) => {
      queryClient.setQueryData(['payment', payment.id], payment);
      navigate(`/success/${orderId}`);
    },
  });

  if (!userId) {
    return (
      <section className="panel">
        <h2>Payment</h2>
        <p>Please sign in to continue.</p>
      </section>
    );
  }

  if (!order) {
    return (
      <section className="panel">
        <h2>Payment</h2>
        <p>We couldn’t find that order in this session.</p>
        <button type="button" onClick={() => navigate('/')}>
          Back to cart
        </button>
      </section>
    );
  }

  const cardComplete =
    card.number.trim().length >= 12 &&
    card.expiry.trim().length >= 4 &&
    card.cvc.trim().length >= 3 &&
    card.holder.trim().length > 0;

  const canPay =
    !payMutation.isPending &&
    (method === 'CARD' ? cardComplete : !walletInsufficient);

  return (
    <section className="panel payment">
      <h2>Pay {formatCents(amountCents)}</h2>
      <p className="payment__order">Order #{order.id}</p>

      <fieldset className="payment__methods">
        <legend>Payment method</legend>
        <label>
          <input
            type="radio"
            name="method"
            value="CARD"
            checked={method === 'CARD'}
            onChange={() => setMethod('CARD')}
          />
          Card
        </label>
        <label>
          <input
            type="radio"
            name="method"
            value="WALLET"
            checked={method === 'WALLET'}
            onChange={() => setMethod('WALLET')}
          />
          Wallet
          {wallet && (
            <small> ({formatCents(wallet.balanceCents, wallet.currency)})</small>
          )}
        </label>
      </fieldset>

      <form
        onSubmit={(e) => {
          e.preventDefault();
          if (canPay) payMutation.mutate();
        }}
      >
        {method === 'CARD' && (
          <div className="payment__card">
            <label htmlFor="card-holder">Name on card</label>
            <input
              id="card-holder"
              autoComplete="cc-name"
              value={card.holder}
              onChange={(e) => setCard({ ...card, holder: e.target.value })}
            />

            <label htmlFor="card-number">Card number</label>
            <input
              id="card-number"
              inputMode="numeric"
              autoComplete="cc-number"
              placeholder="4242 4242 4242 4242"
              value={card.number}
              onChange={(e) => setCard({ ...card, number: e.target.value })}
            />

            <div className="payment__card-row">
              <div>
                <label htmlFor="card-expiry">Expiry</label>
                <input
                  id="card-expiry"
                  autoComplete="cc-exp"
                  placeholder="MM/YY"
                  value={card.expiry}
                  onChange={(e) => setCard({ ...card, expiry: e.target.value })}
                />
              </div>
              <div>
                <label htmlFor="card-cvc">CVC</label>
                <input
                  id="card-cvc"
                  inputMode="numeric"
                  autoComplete="cc-csc"
                  placeholder="123"
                  value={card.cvc}
                  onChange={(e) => setCard({ ...card, cvc: e.target.value })}
                />
              </div>
            </div>
          </div>
        )}

        {method === 'WALLET' && walletInsufficient && (
          <p className="error" role="alert">
            Your wallet balance is too low for this order. Choose card instead.
          </p>
        )}

        <button type="submit" className="primary" disabled={!canPay}>
          {payMutation.isPending
            ? 'Processing…'
            : `Pay ${formatCents(amountCents)}`}
        </button>

        {payMutation.isError && (
          <p className="error" role="alert">
            Payment failed. No charge was made — please try again.
          </p>
        )}
      </form>
    </section>
  );
}
