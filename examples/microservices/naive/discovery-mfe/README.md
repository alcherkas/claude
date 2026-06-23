# discovery-mfe

QuickBite **restaurant discovery** micro-frontend — search, browse, and view
restaurant menus. Part of the QuickBite multi-repo platform (see
`../PLATFORM_SPEC.md`, the canonical contract).

- **Federation name:** `discovery` (Module Federation **remote**, exposes `./App`)
- **Dev port:** `3001`
- **Consumed by:** `shell` host, lazily via `React.lazy(() => import('discovery/App'))`
- **Talks to (through the gateway `http://localhost:8080`):** search, restaurant,
  menu services (per PLATFORM_SPEC §1.2 — the cart/checkout flow is owned by
  `checkout-mfe`, not this MFE)

## Stack

React 18.3 · TypeScript 5.5 · Vite 5.4 · `@originjs/vite-plugin-federation` 1.3.6 ·
React Router 6 · TanStack Query 5 · Axios.

`react`, `react-dom`, and `react-router-dom` are declared as **shared singletons**
so the host and this remote run on a single copy of each.

## API surface (via gateway `/api/*`)

All calls go through the gateway and carry the JWT bearer that the `shell` host
persists in `localStorage` (`quickbite.token`). Endpoint functions live in
`src/api/client.ts`:

| Function | Method & path | Backing service |
|----------|---------------|-----------------|
| `search({ q, cuisine, geo })` | `GET /api/search` | search-service (8084) |
| `listRestaurants(cuisine?)`   | `GET /api/restaurants` | restaurant-service (8082) |
| `getRestaurant(id)`           | `GET /api/restaurants/{id}` | restaurant-service (8082) |
| `getMenu(restaurantId)`       | `GET /api/menu?restaurantId=` | menu-service (8083) |

Data fetching/caching is handled by TanStack Query. discovery-mfe deliberately
does **not** call cart-service: tapping **Add** on a menu item dispatches a
`quickbite:add-to-cart` DOM `CustomEvent` (see `src/session.ts`) that the `shell`
host forwards to `checkout-mfe`, which owns cart-service (`/api/carts/**`).

## Components

- `SearchBar` — query input + cuisine filter, drives `/api/search`.
- `RestaurantList` / `RestaurantCard` — results grid with loading/empty/error states.
- `RestaurantDetail` — restaurant header + embedded `MenuView`.
- `MenuView` — fetches the menu (`/api/menu?restaurantId=`), groups by category,
  and emits the `quickbite:add-to-cart` event on **Add**.
- `MenuItemCard` — a single dish with a quantity stepper and **Add** button that
  hands the item off to `checkout-mfe` via the cross-MFE event.
- `DiscoverPage` — composes `SearchBar` + `RestaurantList` (browse vs. search).

## Develop

```bash
npm install
npm run dev        # http://localhost:3001  (strict port)
```

Run the `shell` host (port 3000) alongside it to see the remote federated in.
Override the gateway with `VITE_GATEWAY_URL` if it is not on `localhost:8080`.

## Build & preview

```bash
npm run build      # tsc + vite build -> dist/ (emits assets/remoteEntry.js)
npm run preview    # serve the built bundle on port 3001
```

## Docker

```bash
docker build -t discovery-mfe .   # node:20 build -> nginx:alpine static serve
```

`nginx.conf` adds permissive CORS on `/assets/` so the host can load
`remoteEntry.js`, and falls back to `index.html` for SPA routes.

## Deploy (Terraform)

`terraform/` provisions an S3 (private) + CloudFront static site via the shared
`react-mfe` module (`../../terraform-modules/modules/react-mfe`), reading
`platform-infra` remote state. Region `us-east-1`, default env `dev`, state key
`discovery-mfe/<env>/terraform.tfstate`.

```bash
cd terraform
terraform init \
  -backend-config="bucket=quickbite-tfstate-dev" \
  -backend-config="key=discovery-mfe/dev/terraform.tfstate" \
  -backend-config="region=us-east-1" \
  -backend-config="dynamodb_table=quickbite-tflocks"
terraform apply
```
