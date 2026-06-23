# payment-service

QuickBite **payment-service** — captures payments for orders. Part of the QuickBite
food-delivery platform (see the root `PLATFORM_SPEC.md`, the canonical contract).

- **Port:** `8089`
- **Database:** `payment_db` (Postgres, Flyway-migrated)
- **Gateway route:** `http://localhost:8080/api/payments/**` → this service
- **Base package:** `com.quickbite.payment`
- **Event role:** Kafka **producer** on `payments.events`

## What it does

When a customer checks out, the checkout flow creates a payment here. The service:

1. Fetches the **authoritative amount** for the order from order-service
   (`GET /internal/orders/{id}`) and verifies the order belongs to the requesting user.
2. Charges the customer:
   - `WALLET` → debits the customer's wallet via wallet-service
     (`POST /internal/wallets/{userId}/debit`).
   - `CARD` → "captures" via a mock card PSP (`CardPspGateway`).
3. Persists a `Payment` row and emits an event:
   - **PaymentCaptured** on success,
   - **PaymentFailed** on failure,
   - **Refunded** when a captured payment is refunded.

Refunds credit the wallet back (wallet payments) or call the mock PSP refund (card payments).

`Payment{id, orderId, userId, amountCents, currency, method(CARD|WALLET),
status(AUTHORIZED|CAPTURED|REFUNDED|FAILED), provider, createdAt}`.

## Dependencies (synchronous, via Feign + Resilience4j circuit breakers)

| Dependency       | Client          | Endpoint hit                                   | Purpose                          |
|------------------|-----------------|------------------------------------------------|----------------------------------|
| order-service    | `OrderClient`   | `GET /internal/orders/{id}`                    | authoritative order amount/owner |
| wallet-service   | `WalletClient`  | `POST /internal/wallets/{userId}/debit`/`credit` | debit on pay, credit on refund |
| identity-service | `IdentityClient`| `GET /internal/users/{id}`                     | user validation                  |

Every client has a fallback that surfaces a `503` via the global error handler. Target URLs come
from `clients.<dep>.url` in `application.yml` (overridable by `CLIENTS_*_URL` env vars).

Cross-service calls only ever hit the target's `/internal/**` endpoints.

## Endpoints

### Public (through the gateway, `/api/payments`)

| Method | Path                        | Description                                          |
|--------|-----------------------------|------------------------------------------------------|
| POST   | `/api/payments`             | Create + capture a payment `{orderId,userId,method}` |
| POST   | `/api/payments/{id}/refund` | Refund a captured payment                            |
| GET    | `/api/payments?orderId=`    | List payments for an order                           |
| GET    | `/api/payments/{id}`        | Fetch a single payment                               |

### Internal (service-to-service, **never** exposed through the gateway)

| Method | Path                          | Description                  |
|--------|-------------------------------|------------------------------|
| GET    | `/internal/payments/{id}`     | Payment by id                |
| GET    | `/internal/payments?orderId=` | Payments for an order        |

### Ops

- Health: `GET /actuator/health`
- OpenAPI UI: `GET /swagger-ui.html` · spec: `GET /v3/api-docs` (committed at `api/openapi.yaml`)

## Events produced (`payments.events`)

Envelope `{eventId,type,occurredAt,payload}`; `payload` carries
`{paymentId,orderId,userId,amountCents,currency,method,status,provider}`.

- `PaymentCaptured` — capture succeeded
- `PaymentFailed` — capture failed
- `Refunded` — captured payment refunded

Consumed downstream by notification-service and order-service.

## Run

```bash
# Postgres + Kafka must be reachable (see root docker-compose.yml for the full stack)
mvn spring-boot:run
# defaults: DB jdbc:postgresql://localhost:5432/payment_db, Kafka localhost:9092
```

Override any value via env vars, e.g. `SERVER_PORT`, `DB_URL`, `DB_USER`, `DB_PASSWORD`,
`KAFKA_BOOTSTRAP_SERVERS`, `CLIENTS_ORDER_URL`, `CLIENTS_WALLET_URL`, `CLIENTS_IDENTITY_URL`,
`KAFKA_TOPIC_PAYMENTS_EVENTS`.

## Build

```bash
mvn verify           # compile + run tests (context-load + MockMvc slice)
docker build -t quickbite/payment-service:local .
```

## Deploy (Terraform)

```bash
cd terraform
terraform init \
  -backend-config="bucket=quickbite-tfstate-dev" \
  -backend-config="dynamodb_table=quickbite-tflocks" \
  -backend-config="region=us-east-1"
terraform apply -var="image_tag=$(git rev-parse --short HEAD)"
```

The Terraform reads `platform-infra` remote state and provisions an ECS Fargate service
(`java-service` module, `path_prefix=/payments`) plus a dedicated Postgres instance
(`postgres-db` module, `payment_db`).
