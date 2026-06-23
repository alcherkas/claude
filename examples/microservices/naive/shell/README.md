# shell — QuickBite host micro-frontend

The **host** of the QuickBite federation (Module Federation **host**, federation
name `shell`, dev port **3000**). It owns the chrome (navigation, auth state,
routing) and lazily composes the six feature MFEs as remotes.

## What it does

- Renders the `Layout` (NavBar with live auth state + footer) around the active
  remote.
- Owns authentication: `AuthProvider` calls `POST /api/auth/login` and
  `/api/auth/register` through the gateway, stores the JWT + user in
  `localStorage`, and exposes them via React context. The axios client attaches
  the bearer token to every request.
- Routes to remotes (lazy `React.lazy` + `Suspense`):

  | Route        | Remote (`./App`)              | Guard                         |
  |--------------|-------------------------------|-------------------------------|
  | `/`, `/discover/*` | `discovery/App`         | —                             |
  | `/checkout/*`| `checkout/App`                | —                             |
  | `/orders/*`  | `orderTracking/App`           | —                             |
  | `/account/*` | `account/App`                 | authenticated                 |
  | `/partner/*` | `restaurantAdmin/App`         | role `RESTAURANT_OWNER`       |
  | `/driver/*`  | `driverPortal/App`            | role `COURIER`                |

- A `RemoteBoundary` error boundary isolates an offline remote so it cannot take
  down the whole shell.

## Federation

This host lists all six remotes by their dev `remoteEntry.js` URL
(`vite.config.ts`):

```
discovery       http://localhost:3001/assets/remoteEntry.js
checkout        http://localhost:3002/assets/remoteEntry.js
orderTracking   http://localhost:3003/assets/remoteEntry.js
account         http://localhost:3004/assets/remoteEntry.js
restaurantAdmin http://localhost:3005/assets/remoteEntry.js
driverPortal    http://localhost:3006/assets/remoteEntry.js
```

Shared singletons: `react`, `react-dom`, `react-router-dom`.

## API

All traffic goes through the API gateway. Base URL comes from
`VITE_GATEWAY_URL` (defaults to `http://localhost:8080`). The shell itself only
talks to identity-service routes:

- `POST /api/auth/login`
- `POST /api/auth/register`
- `GET  /api/auth/me`

## Stack

React 18.3 · TypeScript 5.5 · Vite 5.4 · `@originjs/vite-plugin-federation`
1.3.6 · React Router 6 · TanStack Query 5 · Axios.

## Run / build

```bash
npm install
npm run dev       # vite dev server on :3000
npm run build     # tsc + vite build -> dist/
npm run preview   # serve the built bundle on :3000
```

> The remotes must also be running (ports 3001–3006) for their routes to load;
> otherwise the `RemoteBoundary` shows a fallback.

## Docker

```bash
docker build -t shell .
docker run -p 3000:80 shell   # nginx serves the static bundle
```

## Deploy (Terraform)

Deploys as an S3 (private) + CloudFront static site via the shared `react-mfe`
module. State lives in S3 (`shell/<env>/terraform.tfstate`).

```bash
cd terraform
terraform init \
  -backend-config="bucket=quickbite-tfstate-dev" \
  -backend-config="key=shell/dev/terraform.tfstate" \
  -backend-config="region=us-east-1" \
  -backend-config="dynamodb_table=quickbite-tflocks"
terraform apply
```
