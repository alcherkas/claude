# account-mfe — QuickBite account micro-frontend

The customer **account** feature MFE of the QuickBite federation (Module
Federation **remote**, federation name `account`, dev port **3004**). It exposes
`./App` and is consumed lazily by the `shell` host at `/account/*` (behind an
authentication guard).

## What it does

- **ProfileCard** — shows the signed-in customer (identity-service
  `GET /api/users/me`).
- **WalletPanel** — shows the wallet balance and tops it up
  (wallet-service `GET /api/wallets/{userId}`,
  `POST /api/wallets/{userId}/credits`).
- **OrderHistory** — lists past orders (order-service
  `GET /api/orders?userId=`); delivered orders can be reviewed inline.
- **ReviewForm** — submits a review for a delivered order (review-service
  `POST /api/reviews`).
- **ReviewList** — the customer's submitted reviews (review-service
  `GET /api/reviews?userId=`).

## Federation

Module Federation **remote** (`vite.config.ts`):

```
name:     account
filename: remoteEntry.js
exposes:  './App' -> ./src/App.tsx
shared:   react, react-dom, react-router-dom   (singletons)
```

The shell host loads this remote from
`http://localhost:3004/assets/remoteEntry.js`. When run standalone, `main.tsx`
mounts the same `App` under its own `QueryClientProvider` + `BrowserRouter`
(basename `/account`).

## API

All traffic goes through the API gateway. Base URL comes from
`VITE_GATEWAY_URL` (defaults to `http://localhost:8080`). The axios client
attaches the JWT bearer from `localStorage` (`quickbite.token`, shared with the
shell). Endpoints used (PLATFORM_SPEC §4):

| Function       | Method & path                          | Service            |
|----------------|----------------------------------------|--------------------|
| `getMe`        | `GET  /api/users/me`                   | identity-service   |
| `getWallet`    | `GET  /api/wallets/{userId}`           | wallet-service     |
| `addCredit`    | `POST /api/wallets/{userId}/credits`   | wallet-service     |
| `listOrders`   | `GET  /api/orders?userId=`             | order-service      |
| `myReviews`    | `GET  /api/reviews?userId=`            | review-service     |
| `submitReview` | `POST /api/reviews`                    | review-service     |

## Stack

React 18.3 · TypeScript 5.5 · Vite 5.4 · `@originjs/vite-plugin-federation`
1.3.6 · React Router 6 · TanStack Query 5 · Axios.

## Run / build

```bash
npm install
npm run dev       # vite dev server on :3004
npm run build     # tsc + vite build -> dist/ (incl. remoteEntry.js)
npm run preview   # serve the built bundle on :3004
```

## Docker

```bash
docker build -t account-mfe .
docker run -p 3004:80 account-mfe   # nginx serves the static bundle
```

## Deploy (Terraform)

Deploys as an S3 (private) + CloudFront static bundle via the shared `react-mfe`
module. State lives in S3 (`account-mfe/<env>/terraform.tfstate`).

```bash
cd terraform
terraform init \
  -backend-config="bucket=quickbite-tfstate-dev" \
  -backend-config="key=account-mfe/dev/terraform.tfstate" \
  -backend-config="region=us-east-1" \
  -backend-config="dynamodb_table=quickbite-tflocks"
terraform apply
```
