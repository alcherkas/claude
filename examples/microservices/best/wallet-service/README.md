# wallet-service

Part of the **QuickBite** food-delivery platform (see `../PLATFORM_SPEC.md`, the canonical contract).

## What & why

`wallet-service` owns each customer's in-app wallet: a single balance per user plus an
append-only transaction ledger. Customers top up their wallet (credits); `payment-service`
debits it when a customer chooses to pay for an order with their wallet balance. Debits that
would drive the balance below zero are **rejected**, so a wallet payment fails cleanly rather
than going negative.

- **Port:** `8090`
- **Database:** `wallet_db` (Postgres, Flyway-migrated)
- **Base package:** `com.quickbite.wallet`
- **Gateway route:** `/api/wallets/**` → wallet-service
- **Event role:** none (no Kafka producer/consumer)

### Domain
- `Wallet { userId (PK, identity user UUID), balanceCents, currency, updatedAt }`
- `WalletTxn { id, userId, deltaCents (+credit / -debit), reason, createdAt }`

Balances are integer cents; the DB enforces a non-negative balance via a check constraint and
the service serialises concurrent mutations per user with a pessimistic row lock.

## Dependencies

| Dependency        | Why                                                   | How                                                        |
|-------------------|-------------------------------------------------------|------------------------------------------------------------|
| `identity-service`| Validate that a user exists before opening a wallet   | Feign `IdentityClient` → `GET /internal/users/{id}/validate` |

Identity is an **optional guard**: the Feign client is wrapped in a Resilience4j circuit breaker
and a fallback that fails *open* (allows the credit) on a transient identity outage, and fails
*closed* (404 → user invalid) on a genuine "user not found".

`payment-service` depends on **this** service (calls our `/internal/wallets/**`).

## Endpoints

### Public (through the gateway at `/api/wallets`)
| Method | Path                          | Description                                  |
|--------|-------------------------------|----------------------------------------------|
| GET    | `/api/wallets/{userId}`       | Get a user's wallet balance                  |
| POST   | `/api/wallets/{userId}/credits` | Credit (top up) a wallet `{amountCents,reason}` |

### Internal (service-to-service only — never exposed through the gateway)
| Method | Path                              | Description                                          |
|--------|-----------------------------------|------------------------------------------------------|
| GET    | `/internal/wallets/{userId}`      | Get a user's wallet (used by payment-service)        |
| POST   | `/internal/wallets/{userId}/debit`| Debit a wallet `{amountCents,orderId}`; **409** on insufficient funds |

### Ops
- Health: `GET /actuator/health`
- OpenAPI UI: `GET /swagger-ui.html` · spec: `GET /v3/api-docs` (committed at `api/openapi.yaml`)

Errors use the platform's `{timestamp,status,error,message,path}` shape via a
`@RestControllerAdvice`.

## Run locally

```bash
# Postgres (matches application.yml defaults)
docker run --rm -p 5432:5432 \
  -e POSTGRES_DB=wallet_db -e POSTGRES_USER=wallet -e POSTGRES_PASSWORD=wallet \
  postgres:16

# Run the service (Flyway applies V1__init.sql on boot)
mvn spring-boot:run
```

Override config via env vars: `SERVER_PORT`, `DB_URL`, `DB_USER`, `DB_PASSWORD`, `IDENTITY_URL`.

## Build & test

```bash
mvn -B verify            # compile + run tests
docker build -t quickbite/wallet-service:local .
```

CI (`.github/workflows/ci.yml`) runs `mvn -B verify` then `docker build` on JDK 21.

## Deploy

Terraform in `terraform/` reads `platform-infra` remote state and consumes the shared
`terraform-modules` (`java-service` + `postgres-db`). The ALB listener rule uses
`path_prefix = "/wallets"` to match the gateway route.

```bash
cd terraform
terraform init \
  -backend-config="bucket=quickbite-tfstate-dev" \
  -backend-config="dynamodb_table=quickbite-tflocks" \
  -backend-config="region=us-east-1"
terraform apply -var="image_tag=$(git rev-parse --short HEAD)"
```
