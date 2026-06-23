# order-service

The **central checkout hub** of the QuickBite food-delivery platform. It turns a customer's
cart into a priced, persisted order and is the high-fan-in coordinator between cart, pricing,
restaurant, menu and identity. It is the source of truth for order state and the producer of
`orders.events`.

- **Port:** `8088`
- **Database:** `order_db` (Postgres + Flyway)
- **Base package:** `com.quickbite.order`
- **Gateway route:** `/api/orders/**` → order-service

## What it does

- **Create** an order from the user's immutable cart snapshot, validate the user and restaurant,
  fetch an authoritative pricing quote, persist the order (status `CREATED`) with its items and a
  pricing snapshot, then emit `OrderCreated`.
- **Read / update** orders publicly; a status change emits `OrderStatusChanged`.
- **Expose** a thin internal summary (id, user, restaurant, status, total, currency) consumed by
  payment-, delivery- and review-service.
- **React** to upstream events: `PaymentCaptured` → `CONFIRMED`; delivery `PICKED_UP` / `DELIVERED`
  → corresponding order status.

### Order creation flow (`POST /api/orders`)

1. `IdentityClient` validates the user (`/internal/users/{id}`).
2. `CartClient` pulls the snapshot (`/internal/carts/{userId}/snapshot`).
3. `RestaurantClient` confirms the restaurant is `ACTIVE` (`/internal/restaurants/{id}`).
4. `PricingClient` computes the quote (`POST /internal/pricing/quote`).
5. Order + items + pricing snapshot persisted with status `CREATED`.
6. `OrderCreated` emitted on `orders.events`.

## Dependencies

| Dependency          | Client            | Target endpoint                              | URL property             |
|---------------------|-------------------|----------------------------------------------|--------------------------|
| cart-service        | `CartClient`      | `GET /internal/carts/{userId}/snapshot`      | `clients.cart.url`       |
| pricing-service     | `PricingClient`   | `POST /internal/pricing/quote`               | `clients.pricing.url`    |
| restaurant-service  | `RestaurantClient`| `GET /internal/restaurants/{id}`             | `clients.restaurant.url` |
| menu-service        | `MenuClient`      | `GET /internal/menu-items/{id}`              | `clients.menu.url`       |
| identity-service    | `IdentityClient`  | `GET /internal/users/{id}`                   | `clients.identity.url`   |

Every Feign client has a Resilience4j circuit breaker and a fallback that surfaces a
`503 Service Unavailable` rather than corrupting an order.

### Eventing (Kafka)

| Topic               | Role     | Events                                   |
|---------------------|----------|------------------------------------------|
| `orders.events`     | producer | `OrderCreated`, `OrderStatusChanged`     |
| `payments.events`   | consumer | `PaymentCaptured` → order `CONFIRMED`    |
| `deliveries.events` | consumer | `DeliveryStatusChanged` → `PICKED_UP`/`DELIVERED` |

Events use the envelope `{eventId,type,occurredAt,payload}`. Consumers log-and-skip on errors.

## Endpoints

### Public (via gateway `/api/orders`)

| Method | Path                       | Description                                  |
|--------|----------------------------|----------------------------------------------|
| POST   | `/api/orders`              | Create an order `{userId, promoCode?}`       |
| GET    | `/api/orders/{id}`         | Get an order by id                           |
| GET    | `/api/orders?userId=`      | List a user's orders (newest first)          |
| PATCH  | `/api/orders/{id}/status`  | Update status `{status}` (emits event)       |

### Internal (never exposed through the gateway)

| Method | Path                       | Returns                                                  |
|--------|----------------------------|----------------------------------------------------------|
| GET    | `/internal/orders/{id}`    | `{id,userId,restaurantId,status,totalCents,currency}`    |

### Ops

- Health: `GET /actuator/health`
- OpenAPI: `GET /v3/api-docs`, Swagger UI: `/swagger-ui.html`, committed contract: `api/openapi.yaml`

## Run & build

```bash
# Build + test
mvn -B verify

# Run locally (defaults to localhost dependencies + Postgres on 5432/order_db, Kafka on 9092)
mvn spring-boot:run

# Build the container image
docker build -t quickbite/order-service:local .
docker run -p 8088:8088 quickbite/order-service:local
```

Key environment overrides (all have local defaults): `SERVER_PORT`, `DB_URL`, `DB_USER`,
`DB_PASSWORD`, `KAFKA_BOOTSTRAP_SERVERS`, `CLIENTS_CART_URL`, `CLIENTS_PRICING_URL`,
`CLIENTS_MENU_URL`, `CLIENTS_RESTAURANT_URL`, `CLIENTS_IDENTITY_URL`.

## Deploy (Terraform)

```bash
cd terraform
terraform init \
  -backend-config="bucket=quickbite-tfstate-dev" \
  -backend-config="dynamodb_table=quickbite-tflocks" \
  -backend-config="region=us-east-1"
terraform apply -var="image_tag=$(git rev-parse --short HEAD)"
```

Terraform reads `platform-infra` remote state and consumes the reusable
`java-service` and `postgres-db` modules from `terraform-modules`. The service is published behind
the shared ALB at `path_prefix = /orders`.

## Stack

Java 21 · Spring Boot 3.3.2 · Spring Cloud 2023.0.3 · Spring Data JPA + Flyway + Postgres ·
OpenFeign + Resilience4j · Spring Kafka · springdoc-openapi 2.6.0 · Lombok.
