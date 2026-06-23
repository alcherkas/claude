# pricing-service

QuickBite **pricing-service** — a **stateless** quote engine (port **8086**).

## What & why

Given a cart (`userId`, `restaurantId`, a list of `{menuItemId, qty}` and an optional
`promoCode`), pricing-service produces an authoritative price quote. It is the single
place that knows the platform's fee policy, so order-service never has to re-derive
totals — it simply persists the snapshot pricing returns.

It owns **no database** (no JPA, no Flyway): every input it needs is fetched live from
its dependencies, and the result is computed on the fly.

### Pricing logic

1. For each requested item, fetch the **authoritative price** from menu-service
   (`GET /internal/menu-items/{id}`). Items are validated to belong to the requested
   restaurant and to be available; the menu item's currency is used for the quote.
2. `subtotalCents` = Σ `unitPriceCents * qty`.
3. If a `promoCode` is present, call promotion-service
   (`POST /internal/promotions/apply`) to reserve a redemption and obtain the
   `discountCents` (and possibly `freeDelivery`). Discount is capped at the subtotal.
4. Fees (integer-cents arithmetic):
   - `deliveryFeeCents` — flat **299** (waived when the promotion grants free delivery)
   - `serviceFeeCents` — **10%** of subtotal, capped at **500**
   - `taxCents` — **8%** of the taxable base (`subtotal − discount`, floored at 0)
5. `totalCents` = `(subtotal − discount) + delivery + service + tax`.

A failure in **menu-service** fails the quote (503) — prices cannot be guessed.
A failure in **promotion-service** degrades gracefully: the cart is priced with no
discount. Both behaviours are driven by Resilience4j circuit-breaker fallbacks.

## Dependencies (sync, Feign)

| Client            | Target service     | Endpoint used                          | URL property            |
|-------------------|--------------------|----------------------------------------|-------------------------|
| `MenuClient`      | menu-service :8083 | `GET /internal/menu-items/{id}`        | `clients.menu.url`      |
| `PromotionClient` | promotion-service :8087 | `POST /internal/promotions/apply` | `clients.promotion.url` |

Each client targets only the dependency's `/internal/**` endpoints, has a Resilience4j
circuit breaker, and a fallback. There is **no** event role (no Kafka).

## Endpoints

| Method | Path                      | Exposure            | Description                          |
|--------|---------------------------|---------------------|--------------------------------------|
| POST   | `/api/pricing/quote`      | Public (via gateway `/api/pricing/**`) | Price a cart |
| POST   | `/internal/pricing/quote` | Internal only (never gateway-exposed)  | Same logic, used by order-service |
| GET    | `/actuator/health`        | Internal            | Liveness/readiness                   |
| GET    | `/swagger-ui.html`        | —                   | Swagger UI (`/v3/api-docs` for the spec) |

### Request / response

Request:

```json
{
  "userId": 7,
  "restaurantId": 3,
  "items": [{ "menuItemId": 42, "qty": 2 }],
  "promoCode": "WELCOME10"
}
```

Response:

```json
{
  "subtotalCents": 2000,
  "deliveryFeeCents": 299,
  "serviceFeeCents": 200,
  "taxCents": 144,
  "discountCents": 200,
  "totalCents": 2443,
  "currency": "USD",
  "lineItems": [
    { "menuItemId": 42, "name": "Margherita", "qty": 2, "unitPriceCents": 1000, "lineTotalCents": 2000 }
  ]
}
```

The committed contract lives in [`api/openapi.yaml`](api/openapi.yaml).

## Configuration

All values are overridable via environment variables (see `src/main/resources/application.yml`):

| Env var                 | Default                   | Purpose                       |
|-------------------------|---------------------------|-------------------------------|
| `SERVER_PORT`           | `8086`                    | HTTP port                     |
| `CLIENTS_MENU_URL`      | `http://localhost:8083`   | menu-service base URL         |
| `CLIENTS_PROMOTION_URL` | `http://localhost:8087`   | promotion-service base URL    |

## Run

```bash
# Local (needs menu-service + promotion-service reachable, or rely on the fallbacks)
mvn spring-boot:run

# Quote a cart
curl -s -X POST http://localhost:8086/api/pricing/quote \
  -H 'Content-Type: application/json' \
  -d '{"userId":7,"restaurantId":3,"items":[{"menuItemId":42,"qty":2}],"promoCode":"WELCOME10"}'
```

Through the full local stack use the gateway: `http://localhost:8080/api/pricing/quote`.

## Build

```bash
mvn -B verify            # compile + run tests
docker build -t pricing-service:local .
```

## Deploy

Terraform under [`terraform/`](terraform/) deploys the service to ECS Fargate behind the
shared ALB via the reusable `java-service` module (no `postgres-db` module — stateless).

```bash
cd terraform
terraform init \
  -backend-config="bucket=quickbite-tfstate-dev" \
  -backend-config="dynamodb_table=quickbite-tflocks" \
  -backend-config="region=us-east-1"
terraform apply -var="image_tag=$(git rev-parse --short HEAD)"
```

The ALB listener rule is attached on `path_prefix = /api/pricing`, mirroring the gateway
route in `PLATFORM_SPEC.md §4`.
