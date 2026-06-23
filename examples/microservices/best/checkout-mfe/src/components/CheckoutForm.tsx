import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { getCart, placeOrder } from '../api/client';
import type { Cart, Order, Quote } from '../types';
import { currentUserId } from '../lib/session';
import { OrderSummary } from './OrderSummary';

// Step 2 of the flow: confirm the priced cart, capture delivery details, and
// create the order (order-service snapshots cart + pricing). On success we
// advance to payment carrying the created order.
export function CheckoutForm() {
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const userId = currentUserId();

  const [notes, setNotes] = useState('');

  const quote = queryClient.getQueryData<Quote>(['quote', userId]);

  const cartQuery = useQuery({
    queryKey: ['cart', userId],
    queryFn: () => getCart(userId as string),
    enabled: Boolean(userId),
  });
  const cart: Cart | undefined = cartQuery.data;

  const orderMutation = useMutation({
    mutationFn: () =>
      placeOrder({
        userId: userId as string,
        restaurantId: cart!.restaurantId,
        // The promo applied during pricing is re-resolved server-side from the
        // cart; pricing already baked the discount into the quote snapshot.
      }),
    onSuccess: (order: Order) => {
      queryClient.setQueryData(['order', order.id], order);
      navigate(`/payment/${order.id}`);
    },
  });

  if (!userId) {
    return (
      <section className="panel">
        <h2>Checkout</h2>
        <p>Please sign in to continue.</p>
      </section>
    );
  }

  if (!quote || !cart) {
    return (
      <section className="panel">
        <h2>Checkout</h2>
        <p>Your order needs pricing first.</p>
        <button type="button" onClick={() => navigate('/')}>
          Back to cart
        </button>
      </section>
    );
  }

  return (
    <section className="checkout-grid">
      <form
        className="panel"
        onSubmit={(e) => {
          e.preventDefault();
          orderMutation.mutate();
        }}
      >
        <h2>Checkout</h2>

        <label htmlFor="delivery-notes">Delivery notes (optional)</label>
        <textarea
          id="delivery-notes"
          rows={3}
          placeholder="Gate code, drop-off instructions…"
          value={notes}
          onChange={(e) => setNotes(e.target.value)}
        />

        <div className="checkout-actions">
          <button type="button" onClick={() => navigate('/')}>
            Back
          </button>
          <button
            type="submit"
            className="primary"
            disabled={orderMutation.isPending}
          >
            {orderMutation.isPending ? 'Placing order…' : 'Place order'}
          </button>
        </div>

        {orderMutation.isError && (
          <p className="error" role="alert">
            We couldn’t place your order. Please try again.
          </p>
        )}
      </form>

      <OrderSummary quote={quote} title="Confirm & pay" />
    </section>
  );
}
