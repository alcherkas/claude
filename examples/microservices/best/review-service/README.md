# review-service

QuickBite's **review** service. Captures customer ratings (1–5) and free-text comments for the
restaurant (and optionally the driver) tied to a completed order. A review is only accepted once the
underlying order is **DELIVERED** and **belongs to the reviewing user**, verified live against
`order-service`.

- **Port:** `8094`
- **Database:** `review_db` (Postgres, Flyway-migrated)
- **Base package:** `com.quickbite.review`
- **Gateway path:** `/api/reviews/**` (see PLATFORM_SPEC §4)
- **Event role:** none (no Kafka)

## What it does / why

Closes the QuickBite happy-path loop (PLATFORM_SPEC §6, step 7): after delivery, the customer leaves a
review. The service trusts the authoritative order record for ownership, delivery state, and the
`restaurantId` rather than anything supplied by the caller, and enforces one review per order.

## Domain

`Review{id, orderId (unique), userId, restaurantId, driverId?, rating(1–5), comment, createdAt}`.

## Dependencies (sync, Feign → `/internal/**` only)

| Client            | Target              | Property                  | Default                  | Required | Purpose                                            |
|-------------------|---------------------|---------------------------|--------------------------|----------|----------------------------------------------------|
| `OrderClient`     | order-service       | `clients.order.url`       | `http://localhost:8088`  | yes      | `GET /internal/orders/{id}` — verify DELIVERED + owner |
| `RestaurantClient`| restaurant-service  | `clients.restaurant.url`  | `http://localhost:8082`  | optional | `GET /internal/restaurants/{id}` — optional enrichment |
| `IdentityClient`  | identity-service    | `clients.identity.url`    | `http://localhost:8081`  | yes      | `GET /internal/users/{id}` — confirm reviewing user is active |

Each Feign client has a Resilience4j circuit breaker and a fallback. The mandatory `OrderClient`
and `IdentityClient` fallbacks raise `503`; the optional `RestaurantClient` fallback degrades to
`null` so reviews still succeed when restaurant-service is down.

## Endpoints

### Public (`/api/reviews`, via gateway)
The authenticated user id arrives in the `X-User-Id` header injected by the gateway after JWT validation.

| Method | Path                              | Description                                                        |
|--------|-----------------------------------|-------------------------------------------------------------------|
| POST   | `/api/reviews`                    | Create a review `{orderId, rating, comment}` for a delivered order |
| GET    | `/api/reviews?restaurantId={id}`  | List reviews for a restaurant (newest first)                      |
| GET    | `/api/reviews?driverId={id}`      | List reviews for a driver (newest first)                          |
| GET    | `/api/reviews?userId={id}`        | List reviews written by a user (newest first)                     |

`GET /api/reviews` requires **exactly one** of `restaurantId` / `driverId` / `userId` (else `422`).

### Internal (`/internal/reviews`, never exposed through the gateway)

| Method | Path                                   | Description                          |
|--------|----------------------------------------|--------------------------------------|
| GET    | `/internal/reviews/{id}`               | Fetch a single review                |
| GET    | `/internal/reviews?restaurantId={id}`  | List a restaurant's reviews          |

### Ops
- Health: `GET /actuator/health`
- OpenAPI UI: `GET /swagger-ui.html`; spec at `GET /v3/api-docs` (committed as `api/openapi.yaml`)

## Error format

`@RestControllerAdvice` returns `{timestamp, status, error, message, path}`. Validation → `400`,
business rule violations (not delivered / wrong owner / already reviewed) → `422`, missing review →
`404`, dependency outage → `503`.

## Run

```bash
# Postgres must be reachable at jdbc:postgresql://localhost:5432/review_db
mvn spring-boot:run
# or override via env vars:
SERVER_PORT=8094 DB_URL=jdbc:postgresql://localhost:5432/review_db \
  CLIENTS_ORDER_URL=http://localhost:8088 \
  CLIENTS_RESTAURANT_URL=http://localhost:8082 \
  CLIENTS_IDENTITY_URL=http://localhost:8081 \
  mvn spring-boot:run
```

## Build & test

```bash
mvn -B verify          # compile + unit/slice tests (H2 + mocked Feign clients)
docker build -t quickbite/review-service:local .
```

## Deploy

Terraform in `terraform/` reads `platform-infra` remote state and consumes the shared
`java-service` + `postgres-db` modules from `terraform-modules`:

```bash
cd terraform
terraform init \
  -backend-config="bucket=quickbite-tfstate-dev" \
  -backend-config="dynamodb_table=quickbite-tflocks" \
  -backend-config="region=us-east-1"
terraform apply -var="image_tag=$(git rev-parse --short HEAD)"
```

The ALB listener rule binds `path_prefix = "/reviews"`, and the `environment` map mirrors
`application.yml` (DB + `CLIENTS_*` URLs).
