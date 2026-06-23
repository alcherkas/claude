# promotion-service

QuickBite's promotion engine. Owns promo codes, validates them at checkout, and reserves
redemptions when an order is placed.

- **Port:** `8087`
- **Database:** `promotion_db` (Postgres + Flyway)
- **Base package:** `com.quickbite.promotion`
- **Gateway route:** `/api/promotions/**` → this service
- **Event role:** none (no Kafka)

## What & why

Customers apply discount codes during checkout. This service is the single authority for
whether a code is currently usable for a given subtotal/user, how big the discount is, and
how many times a code may be redeemed (globally and per user). `pricing-service` and
`order-service` call the internal `apply` endpoint to atomically reserve a redemption when
an order is created, keeping the redemption ledger consistent.

### Domain

- **Promotion** — `id, code (unique), type {PERCENT|FIXED|FREE_DELIVERY}, value,
  minSubtotalCents, validFrom, validTo, maxRedemptions, perUserLimit, active`.
  - `PERCENT`: `value` is a whole percentage (0-100) of the subtotal.
  - `FIXED`: `value` is a discount in cents (capped at the subtotal).
  - `FREE_DELIVERY`: flags a waived delivery fee; the discount on the subtotal is 0 and the
    fee is removed downstream by `pricing-service`.
- **Redemption** — `id, promotionId, userId, orderId, redeemedAt`. Unique per
  `(promotionId, orderId)` so re-applying for the same order is idempotent.

## Dependencies

| Kind | Target            | Why                                                            |
|------|-------------------|---------------------------------------------------------------|
| Sync | `identity-service` (optional) | Verifies a presented `userId` exists before reserving a redemption. Treated as optional: if identity is unreachable the circuit-breaker fallback lets validation/apply proceed. |

Inter-service calls use a Feign client (`IdentityClient`) wrapped in a Resilience4j circuit
breaker, targeting identity's `/internal/**` endpoints via `clients.identity.url`.

## Endpoints

### Public (via gateway under `/api/promotions`)

| Method | Path                                                        | Description |
|--------|-------------------------------------------------------------|-------------|
| POST   | `/api/promotions`                                           | Create a promotion. |
| GET    | `/api/promotions`                                           | List all promotions. |
| GET    | `/api/promotions/validate?code=&subtotalCents=&userId=`     | Validate a code; returns `{valid, type, discountCents, reason}`. Does not reserve. |

### Internal (service-to-service only, never exposed via gateway)

| Method | Path                          | Description |
|--------|-------------------------------|-------------|
| POST   | `/internal/promotions/apply`  | Body `{code, userId, subtotalCents, orderId}`. Reserves a redemption and returns `{redemptionId, type, discountCents}`. Idempotent per `(code, orderId)`. Used by pricing/order. |

### Operational

- Health: `GET /actuator/health`
- OpenAPI UI: `GET /swagger-ui.html`; spec at `GET /v3/api-docs` (committed at `api/openapi.yaml`).

All errors return `{timestamp, status, error, message, path}`.

## Run locally

Requires a Postgres `promotion_db`:

```bash
docker run --rm -e POSTGRES_DB=promotion_db -e POSTGRES_USER=promotion \
  -e POSTGRES_PASSWORD=promotion -p 5432:5432 postgres:16

mvn spring-boot:run
```

Override config via env vars (`SERVER_PORT`, `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`,
`IDENTITY_URL`).

Example:

```bash
curl -X POST localhost:8087/api/promotions -H 'Content-Type: application/json' -d '{
  "code":"WELCOME10","type":"PERCENT","value":10,"minSubtotalCents":1000,
  "validFrom":"2026-01-01T00:00:00Z","validTo":"2026-12-31T23:59:59Z",
  "maxRedemptions":1000,"perUserLimit":1,"active":true
}'

curl "localhost:8087/api/promotions/validate?code=WELCOME10&subtotalCents=2500&userId=42"
```

## Build & test

```bash
mvn -B verify        # compiles, runs context-load + MockMvc slice tests
```

## Docker

```bash
docker build -t quickbite/promotion-service:local .
docker run --rm -p 8087:8087 \
  -e DB_URL=jdbc:postgresql://host.docker.internal:5432/promotion_db \
  quickbite/promotion-service:local
```

## Deploy (Terraform)

`terraform/` provisions an ECS Fargate service behind the shared ALB (path
`/api/promotions`) plus a dedicated Postgres instance (`promotion_db`). It reads
`platform-infra` remote state and consumes the reusable `java-service` and `postgres-db`
modules from `terraform-modules`.

```bash
cd terraform
terraform init \
  -backend-config="bucket=quickbite-tfstate-dev" \
  -backend-config="key=promotion-service/dev/terraform.tfstate" \
  -backend-config="region=us-east-1" \
  -backend-config="dynamodb_table=quickbite-tflocks"
terraform apply -var="image_tag=$(git rev-parse --short HEAD)"
```
