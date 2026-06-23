# restaurant-admin-mfe

QuickBite **restaurant owner dashboard** — a React micro-frontend (Module
Federation **remote**, federation name `restaurantAdmin`) where restaurant
owners manage their restaurants, menus, incoming orders, and promotions.

Part of the QuickBite multi-repo platform. The canonical contract is
[`PLATFORM_SPEC.md`](../PLATFORM_SPEC.md); this repo conforms to §1.2, §2.3,
§2.4 and the gateway routes in §4.

## Role in the platform

| Property            | Value                                            |
|---------------------|--------------------------------------------------|
| Federation name     | `restaurantAdmin`                                |
| Dev port            | `3005`                                            |
| Module type         | Remote — exposes `./App` (`./src/App.tsx`)       |
| Host                | `shell` (port 3000) loads it lazily              |
| Talks to (via gateway) | restaurant, menu, order, promotion services    |

The `shell` host consumes this remote from
`http://localhost:3005/assets/remoteEntry.js`. Shared singletons (`react`,
`react-dom`, `react-router-dom`) come from the host so only one copy of each is
loaded.

## Tech stack (pinned)

React 18.3 · TypeScript 5.5 · Vite 5.4 · `@originjs/vite-plugin-federation`
1.3.6 · React Router 6 · TanStack Query 5 · Axios.

## Backend endpoints used

All requests go through the API gateway at
`import.meta.env.VITE_GATEWAY_URL` (default `http://localhost:8080`). The JWT
bearer is read from `localStorage` (`quickbite.token`) and attached to every
request by an Axios interceptor.

| Function             | Method & path                            | Service            |
|----------------------|------------------------------------------|--------------------|
| `myRestaurants`      | `GET /api/restaurants`                   | restaurant-service |
| `createRestaurant`   | `POST /api/restaurants`                  | restaurant-service |
| `setStatus`          | `PATCH /api/restaurants/{id}/status`     | restaurant-service |
| `listMenu`           | `GET /api/menu?restaurantId=`            | menu-service       |
| `createMenuItem`     | `POST /api/menu`                         | menu-service       |
| `toggleAvailability` | `PATCH /api/menu/{id}/availability`      | menu-service       |
| `restaurantOrders`   | `GET /api/orders?restaurantId=&status=`  | order-service      |
| `advanceOrder`       | `PATCH /api/orders/{id}/status`          | order-service      |
| `promotions`         | `GET /api/promotions?restaurantId=`      | promotion-service  |
| `createPromotion`    | `POST /api/promotions`                   | promotion-service  |

## Feature components

- `RestaurantDashboard` — lists the owner's restaurants, creates new ones, and
  toggles ACTIVE/SUSPENDED status. Chooses the "active" restaurant the other
  pages operate on.
- `MenuEditor` + `MenuItemForm` — view the active restaurant's menu, add items,
  and flip item availability.
- `IncomingOrders` — live (polled) queue of active orders; advance them through
  CREATED → CONFIRMED → PREPARING → READY or cancel pre-pickup.
- `PromotionManager` + `PromotionForm` — list and create PERCENT / FIXED /
  FREE_DELIVERY promotions scoped to the active restaurant.

## Run / build

```bash
npm install        # not run by CI generators — install locally
npm run dev        # vite dev server on http://localhost:3005 (standalone)
npm run build      # tsc + vite build -> dist/ (emits assets/remoteEntry.js)
npm run preview    # serve the production build on :3005
```

Run standalone for development, or run the `shell` host (port 3000) alongside it
to see the remote composed into the full app.

### Environment

| Variable           | Default                  | Purpose                     |
|--------------------|--------------------------|-----------------------------|
| `VITE_GATEWAY_URL` | `http://localhost:8080`  | API gateway base URL        |

## Docker

```bash
docker build -t restaurant-admin-mfe .
docker run -p 3005:80 restaurant-admin-mfe   # nginx serves the static bundle
```

Multi-stage build: `node:20-alpine` builds the federated bundle, `nginx:alpine`
serves it. `nginx.conf` adds CORS headers on `/assets/` so the host can load
`remoteEntry.js` cross-origin, and falls back to `index.html` for SPA routes.

## Deploy (Terraform)

`terraform/` deploys the bundle as an S3 + CloudFront static site via the shared
`react-mfe` module from `terraform-modules`. State lives in the S3 backend
(`quickbite-tfstate-<env>`, key `restaurant-admin-mfe/<env>/terraform.tfstate`)
with DynamoDB locking.

```bash
cd terraform
terraform init \
  -backend-config="bucket=quickbite-tfstate-dev" \
  -backend-config="key=restaurant-admin-mfe/dev/terraform.tfstate" \
  -backend-config="region=us-east-1" \
  -backend-config="dynamodb_table=quickbite-tflocks"
terraform apply
```
