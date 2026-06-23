# cart-service

Part of the **QuickBite** food-delivery platform (see `../PLATFORM_SPEC.md`, the canonical contract).

## What & why

`cart-service` owns the customer shopping cart. There is exactly **one cart per user**
(`userId` is the primary key). A cart is **restaurant-scoped**: every line item must belong
to the same restaurant, and any attempt to mix restaurants is rejected. Whenever an item is
added, its name and price are **re-fetched authoritatively from menu-service** — the client is
never trusted for prices. Checkout produces an **immutable snapshot** that order-service later
consumes to build an order.

- **Port:** `8085`
- **Database:** `cart_db` (Postgres, Flyway-migrated)
- **Base package:** `com.quickbite.cart`
- **Gateway path:** `/api/carts/**`
- **Event role:** none (no Kafka)

## Dependencies (sync, via Feign → `/internal/**`)

| Dependency         | Client       | Property            | Default URL              | Used for                         |
|--------------------|--------------|---------------------|--------------------------|----------------------------------|
| `menu-service`     | `MenuClient`     | `clients.menu.url`     | `http://localhost:8083`  | re-pricing items on add                         |
| `identity-service` | `IdentityClient` | `clients.identity.url` | `http://localhost:8081`  | validating the owning user exists/is active     |

Each Feign client is guarded by a Resilience4j circuit breaker plus a fallback that fails
closed (`503`) when the dependency is unreachable, and surfaces a genuine upstream `404`.

## Endpoints

### Public (through the gateway at `/api/carts/**`)

| Method | Path                                  | Description                                                                  |
|--------|---------------------------------------|------------------------------------------------------------------------------|
| GET    | `/api/carts/{userId}`                 | Get the user's cart (returns an empty cart if none exists).                  |
| POST   | `/api/carts/{userId}/items`           | Add an item; re-prices via `MenuClient`; rejects a different restaurant.     |
| PUT    | `/api/carts/{userId}/items/{itemId}`  | Change the quantity of a cart item.                                          |
| DELETE | `/api/carts/{userId}/items/{itemId}`  | Remove an item from the cart.                                                |
| POST   | `/api/carts/{userId}/checkout`        | Return an immutable snapshot `{userId,restaurantId,items[],subtotalCents}`.  |

### Internal (service-to-service only — never exposed through the gateway)

| Method | Path                                    | Description                                          |
|--------|-----------------------------------------|------------------------------------------------------|
| GET    | `/internal/carts/{userId}/snapshot`     | Same snapshot shape, consumed by order-service.      |

### Ops

- Health: `GET /actuator/health`
- OpenAPI UI: `GET /swagger-ui.html` — spec at `GET /v3/api-docs` (committed at `api/openapi.yaml`).

## Error format

All errors return `{timestamp,status,error,message,path}` via a `@RestControllerAdvice`.
`404` = cart/item/menu-item not found or unknown user, `409` = empty cart or
cross-restaurant/unavailable item, `503` = menu-service or identity-service unavailable.

## Run locally

Requires Java 21, Maven, and a Postgres `cart_db` reachable at `localhost:5432`.

```bash
# from this directory
mvn spring-boot:run
# overriding config:
DB_URL=jdbc:postgresql://localhost:5432/cart_db MENU_URL=http://localhost:8083 mvn spring-boot:run
```

The full local stack (Postgres, Kafka, all services) is wired in the root `docker-compose.yml`.

## Build & test

```bash
mvn -B verify        # compile + run tests (context load + MockMvc slice)
mvn -B package       # build the runnable jar in target/
```

## Docker

```bash
docker build -t quickbite/cart-service:local .
docker run --rm -p 8085:8085 \
  -e DB_URL=jdbc:postgresql://host.docker.internal:5432/cart_db \
  -e MENU_URL=http://menu-service:8083 \
  quickbite/cart-service:local
```

## Deploy (Terraform)

`terraform/` deploys cart-service as an ECS Fargate service behind the shared ALB, plus a
`cart_db` Postgres instance. It reads `platform-infra` remote-state outputs and consumes the
reusable `java-service` and `postgres-db` modules from `terraform-modules`.

```bash
cd terraform
terraform init \
  -backend-config="bucket=quickbite-tfstate-dev" \
  -backend-config="dynamodb_table=quickbite-tflocks" \
  -backend-config="region=us-east-1"
terraform apply -var="env=dev" -var="image_tag=$(git rev-parse --short HEAD)"
```
