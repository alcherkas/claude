# driver-portal-mfe

QuickBite **driver portal** micro-frontend — the courier app. Drivers go on/off
shift, accept available deliveries, advance an active delivery through pickup and
drop-off, and watch their earnings.

- **Stack:** React 18.3, TypeScript 5.5, Vite 5.4, `@originjs/vite-plugin-federation` 1.3.6,
  React Router 6, TanStack Query 5, Axios.
- **Module Federation:** REMOTE. Federation name **`driverPortal`**, dev port **3006**,
  exposes `./App` (`./src/App.tsx`). Shared singletons: `react`, `react-dom`,
  `react-router-dom`. The `shell` host (port 3000) consumes
  `http://localhost:3006/assets/remoteEntry.js` lazily.
- **Talks to (via the API gateway at `http://localhost:8080`):** `driver-service`
  (`/api/drivers/**`) and `delivery-service` (`/api/deliveries/**`) — see
  PLATFORM_SPEC §1.2 / §4.

## Gateway endpoints used

| Function          | Method & path                          | Service          |
|-------------------|----------------------------------------|------------------|
| `getDriver`       | `GET /api/drivers/{id}`                 | driver-service   |
| `setAvailability` | `PATCH /api/drivers/{id}/availability`  | driver-service   |
| `pingLocation`    | `POST /api/drivers/{id}/location`       | driver-service   |
| `listDeliveries`  | `GET /api/deliveries?driverId=`         | delivery-service |
| `advanceDelivery` | `PATCH /api/deliveries/{id}/status`     | delivery-service |

The Axios client (`src/api/client.ts`) sets `baseURL` from
`import.meta.env.VITE_GATEWAY_URL` (default `http://localhost:8080`) and attaches the
JWT bearer the shell host persists in `localStorage` under `quickbite.token`.

## Components

- `DriverDashboard` — resolves the courier from the host session and composes the panels.
- `AvailabilityToggle` — go online/offline (`AVAILABLE` ↔ `OFFLINE`).
- `AvailableDeliveries` — claimable `PENDING` deliveries; accept → `ASSIGNED`.
- `ActiveDelivery` — advances the live delivery and pings location while en route.
- `EarningsPanel` — derives completed/active counts and total payout.

## Develop

```bash
npm install
npm run dev       # http://localhost:3006 (standalone)
npm run build     # tsc + vite build -> dist/ (emits assets/remoteEntry.js)
npm run preview   # serve the built bundle on 3006
```

Run alongside the `shell` host (and the other remotes) to use it federated.

## Docker

```bash
docker build -t driver-portal-mfe .
docker run -p 3006:80 driver-portal-mfe   # nginx serves dist/, CORS on /assets/
```

## Deploy (Terraform)

`terraform/` deploys the built bundle as an S3 + CloudFront static site via the
shared `react-mfe` module. Remote state lives in `quickbite-tfstate-<env>` with key
`driver-portal-mfe/<env>/terraform.tfstate`.

```bash
cd terraform
terraform init \
  -backend-config="bucket=quickbite-tfstate-dev" \
  -backend-config="key=driver-portal-mfe/dev/terraform.tfstate" \
  -backend-config="region=us-east-1" \
  -backend-config="dynamodb_table=quickbite-tflocks"
terraform apply
```
