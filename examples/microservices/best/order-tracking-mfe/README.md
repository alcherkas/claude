# order-tracking-mfe

QuickBite **order-tracking** micro-frontend — live order and delivery tracking for
customers. Module Federation **remote**, federation name **`orderTracking`**,
dev port **3003** (see `PLATFORM_SPEC.md` §1.2).

## What it does

Shows a customer's orders and, for a selected order, a live view that combines:

- **OrderList** — the customer's orders, polling for status changes.
- **OrderStatusTimeline** — the order lifecycle (`CREATED → CONFIRMED → PREPARING →
  READY → PICKED_UP → DELIVERED`, with `CANCELLED` terminal).
- **DeliveryTracker** — the courier's delivery status, polling the tracking feed.
- **LiveMap** — a lightweight SVG plot of the courier's tracking points (placeholder
  for a real tile map in production).

`OrderTrackingPage` composes the timeline + delivery tracker for a single order.

## Backend dependencies (via the API gateway)

All requests go through the gateway at `import.meta.env.VITE_GATEWAY_URL`
(default `http://localhost:8080`). The JWT issued by `identity-service` is read from
`localStorage` (`qb_token`) and sent as a bearer token; the gateway validates it.

| Function       | Method & path                              | Service          |
|----------------|--------------------------------------------|------------------|
| `listOrders`   | `GET /api/orders?userId=`                  | order-service    |
| `getOrder`     | `GET /api/orders/{id}`                      | order-service    |
| `getDelivery`  | `GET /api/deliveries?orderId=`             | delivery-service |
| `getTracking`  | `GET /api/deliveries/{id}/tracking`        | delivery-service |

Queries use **TanStack Query** `refetchInterval` (4–5s) so status driven by
`deliveries.events` / `orders.events` surfaces without manual refresh.

## Federation

Exposes `./App` → `./src/App.tsx` as the remote `orderTracking`, sharing
`react`, `react-dom`, `react-router-dom` as singletons. The `shell` host loads this
remote from `http://localhost:3003/assets/remoteEntry.js`.

## Stack

React 18.3, TypeScript 5.5, Vite 5.4, `@originjs/vite-plugin-federation` 1.3.6,
React Router 6, TanStack Query 5, Axios.

## Run / build

```bash
npm install
npm run dev       # standalone on http://localhost:3003
npm run build     # tsc + vite build -> dist/ (incl. assets/remoteEntry.js)
npm run preview   # serve the production build on :3003
```

## Docker

```bash
docker build -t order-tracking-mfe .
docker run -p 3003:80 order-tracking-mfe   # nginx serves dist/, healthz at /healthz
```

## Deploy (Terraform)

Deploys as an S3 (private) + CloudFront static site via the shared
`terraform-modules/modules/react-mfe` module.

```bash
cd terraform
terraform init \
  -backend-config="bucket=quickbite-tfstate-dev" \
  -backend-config="dynamodb_table=quickbite-tflocks" \
  -backend-config="region=us-east-1"
terraform apply
```
