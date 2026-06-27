import { useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  keepPreviousData,
  useMutation,
  useQuery,
  useQueryClient,
} from '@tanstack/react-query';
import {
  getCart,
  quote as requestQuote,
  removeItem,
  updateItem,
} from '../api/client';
import type { Cart, PromoValidation, Quote } from '../types';
import { currentUserId } from '../lib/session';
import { CartItemRow } from './CartItemRow';
import { PromoCodeInput } from './PromoCodeInput';
import { TipInput } from './TipInput';
import { OrderSummary } from './OrderSummary';

// Step 1 of the checkout flow: show the cart, let the customer edit quantities
// and apply a promo, then request an authoritative pricing quote and advance.
export function CartView() {
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const userId = currentUserId();

  const [promo, setPromo] = useState<PromoValidation | null>(null);
  const [tipCents, setTipCents] = useState(0);

  const cartQuery = useQuery({
    queryKey: ['cart', userId],
    queryFn: () => getCart(userId as string),
    enabled: Boolean(userId),
    placeholderData: keepPreviousData,
  });

  const cart = cartQuery.data;
  const subtotalCents = useMemo(
    () =>
      (cart?.items ?? []).reduce(
        (sum, i) => sum + i.unitPriceCents * i.qty,
        0,
      ),
    [cart],
  );

  const mutateItem = useMutation({
    mutationFn: ({ menuItemId, qty }: { menuItemId: string; qty: number }) =>
      qty <= 0
        ? removeItem(userId as string, menuItemId)
        : updateItem(userId as string, menuItemId, { qty }),
    onSuccess: (next: Cart) => {
      queryClient.setQueryData(['cart', userId], next);
    },
  });

  const quoteMutation = useMutation({
    mutationFn: (input: Cart) =>
      requestQuote({
        userId: input.userId,
        restaurantId: input.restaurantId,
        items: input.items.map((i) => ({
          menuItemId: i.menuItemId,
          qty: i.qty,
        })),
        promoCode: promo?.valid ? promo.code : undefined,
        tipCents,
      }),
    onSuccess: (q: Quote) => {
      if (cart) {
        // Hand the priced order to the checkout step via cache.
        queryClient.setQueryData(['quote', userId], q);
        navigate('/checkout');
      }
    },
  });

  if (!userId) {
    return (
      <section className="panel">
        <h2>Your cart</h2>
        <p>Please sign in to view your cart and check out.</p>
      </section>
    );
  }

  if (cartQuery.isLoading) {
    return (
      <section className="panel">
        <h2>Your cart</h2>
        <p>Loading your cart…</p>
      </section>
    );
  }

  if (cartQuery.isError) {
    return (
      <section className="panel">
        <h2>Your cart</h2>
        <p role="alert">We couldn’t load your cart. Please try again.</p>
        <button type="button" onClick={() => cartQuery.refetch()}>
          Retry
        </button>
      </section>
    );
  }

  const empty = !cart || cart.items.length === 0;

  return (
    <section className="checkout-grid">
      <div className="panel">
        <h2>Your cart</h2>

        {empty ? (
          <p>Your cart is empty — add something tasty to get started.</p>
        ) : (
          <>
            <table className="cart-table">
              <thead>
                <tr>
                  <th>Item</th>
                  <th>Qty</th>
                  <th>Total</th>
                  <th aria-hidden="true" />
                </tr>
              </thead>
              <tbody>
                {cart!.items.map((item) => (
                  <CartItemRow
                    key={item.menuItemId}
                    item={item}
                    busy={mutateItem.isPending}
                    onChangeQty={(menuItemId, qty) =>
                      mutateItem.mutate({ menuItemId, qty })
                    }
                    onRemove={(menuItemId) =>
                      mutateItem.mutate({ menuItemId, qty: 0 })
                    }
                  />
                ))}
              </tbody>
            </table>

            <PromoCodeInput
              subtotalCents={subtotalCents}
              appliedCode={promo?.valid ? promo.code : null}
              onApply={setPromo}
              onClear={() => setPromo(null)}
            />

            <TipInput
              subtotalCents={subtotalCents}
              tipCents={tipCents}
              onChange={setTipCents}
            />

            <button
              type="button"
              className="primary"
              disabled={quoteMutation.isPending}
              onClick={() => cart && quoteMutation.mutate(cart)}
            >
              {quoteMutation.isPending ? 'Pricing…' : 'Continue to checkout'}
            </button>

            {quoteMutation.isError && (
              <p className="error" role="alert">
                We couldn’t price your order. Please try again.
              </p>
            )}
          </>
        )}
      </div>

      <OrderSummary
        quote={quoteMutation.data}
        loading={quoteMutation.isPending}
      />
    </section>
  );
}
