# checkout-mfe

QuickBite **checkout** micro-frontend — the cart, checkout, and payment
experience. It is a **Module Federation remote** (federation name `checkout`)
consumed by the `shell` host, and also runs standalone for local development.

- **Dev port:** `3002` (`strictPort`)
- **Federation name:** `checkout`
- **Exposes:** `./App` → `src/App.tsx`
- **Shared singletons:** `react`, `react-dom`, `react-router-dom`
- **Talks to (via the API gateway `http://localhost:8080`):** cart, pricing,
  promotion, order, payment, wallet

See the canonical [`PLATFORM_SPEC.md`](../PLATFORM_SPEC.md) for the authoritative
ports, routes, and contracts.

## Stack

React 18.3 · TypeScript 5.5 · Vite 5.4 · `@originjs/vite-plugin-federation`
1.3.6 · React Router 6 · TanStack Query 5 · Axios.

## Checkout flow

`CartView` → quote → `CheckoutForm` (place order) → `PaymentForm` (pay) →
`OrderSuccess`:

1. **Show cart** — `GET /api/carts/{userId}`; edit quantities
   (`PUT/DELETE /api/carts/{userId}/items/{menuItemId}`).
2. **Quote** — apply a promo (`GET /api/promotions/validate`) then request an
   authoritative price (`POST /api/pricing/quote`).
3. **Place order** — `POST /api/orders` (order-service snapshots the cart +
   pricing and emits `OrderCreated` on `orders.events`).
4. **Pay** — `POST /api/payments` with `CARD` or `WALLET`
   (`GET /api/wallets/{userId}` shows the balance); payment-service emits
   `PaymentCaptured` on `payments.events`.
5. **Success** — confirmation; the customer continues in `order-tracking-mfe`.

The JWT issued by the shell (identity-service) is read from `localStorage`
(`quickbite.token`) and attached as a bearer to every gateway request.

## API surface (`src/api/client.ts`)

| Fn | Method & path |
|----|---------------|
| `getCart(userId)` | `GET /api/carts/{userId}` |
| `updateItem(userId, menuItemId, body)` | `PUT /api/carts/{userId}/items/{menuItemId}` |
| `removeItem(userId, menuItemId)` | `DELETE /api/carts/{userId}/items/{menuItemId}` |
| `validatePromo(code, subtotalCents)` | `GET /api/promotions/validate` |
| `quote(body)` | `POST /api/pricing/quote` |
| `placeOrder(body)` | `POST /api/orders` |
| `pay(body)` | `POST /api/payments` |
| `getWallet(userId)` | `GET /api/wallets/{userId}` |

## Components (`src/components/`)

`CartView`, `CartItemRow`, `PromoCodeInput`, `OrderSummary`, `CheckoutForm`,
`PaymentForm` (plus `OrderSuccess`).

## Scripts

```bash
npm run dev      # vite dev server on :3002
npm run build    # tsc + vite build (emits assets/remoteEntry.js)
npm run preview  # preview the production build on :3002
```

`VITE_GATEWAY_URL` overrides the gateway base URL (defaults to
`http://localhost:8080`).

## Docker

```bash
docker build -t checkout-mfe .
docker run -p 3002:80 checkout-mfe   # nginx serving the static bundle
```

## Terraform

Deploys as an S3 + CloudFront static site via the shared
`../../terraform-modules/modules/react-mfe` module.

```bash
cd terraform
terraform init \
  -backend-config="bucket=quickbite-tfstate-dev" \
  -backend-config="key=checkout-mfe/dev/terraform.tfstate" \
  -backend-config="region=us-east-1" \
  -backend-config="dynamodb_table=quickbite-tflocks"
terraform apply
```
