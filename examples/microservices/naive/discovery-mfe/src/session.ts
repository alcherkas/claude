// Cross-MFE hand-off helpers. discovery-mfe is limited to the search,
// restaurant and menu services (PLATFORM_SPEC §1.2) and does NOT own the cart —
// cart-service (/api/carts/**) belongs to checkout-mfe. When a customer adds an
// item from a menu we dispatch a DOM CustomEvent on `window`; the shell host
// listens for it and routes the customer to checkout-mfe (which holds the
// authenticated user and calls cart-service). This keeps discovery free of any
// auth/user state, which the shell host owns.

import { ADD_TO_CART_EVENT, type AddToCartDetail } from './types';

export function emitAddToCart(detail: AddToCartDetail): void {
  window.dispatchEvent(
    new CustomEvent<AddToCartDetail>(ADD_TO_CART_EVENT, { detail }),
  );
}
