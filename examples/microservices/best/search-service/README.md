# search-service

Discovery / search read model for the QuickBite platform.

## What & why

`search-service` maintains a **denormalized read model** of restaurants and menu items so the
discovery experience can answer "what can I eat near me?" with a single fast query, without
fanning out to `restaurant-service` and `menu-service` at request time.

It is primarily **event-driven**: it consumes `restaurant.events` and `menu.events` from Kafka
and upserts a `search_doc` row per restaurant and per menu item. The public `/api/search`
endpoints then read only from this local index. For resilience it also keeps **Feign clients**
to its declared dependencies (`restaurant-service`, `menu-service`, PLATFORM_SPEC §1.1) and uses
them to backfill / reconcile the index from the dependencies' `/internal/**` endpoints when an
event is missed or arrives out of order.

> The example uses **Postgres `ILIKE` / `pg_trgm`** matching. In production this read model would
> be served by **OpenSearch** (relevance scoring, geo distance sort, typo tolerance); the SQL here
> is the local stand-in.

- **Port**: `8084`
- **Database**: `search_db` (Postgres + Flyway)
- **Package**: `com.quickbite.search`
- **Gateway path**: `/api/search/**`
- **Event role**: **consumer** of `restaurant.events`, `menu.events`

## Dependencies

| Kind   | What                          | Notes                                                            |
|--------|-------------------------------|------------------------------------------------------------------|
| Kafka  | `restaurant.events` (consume) | `RestaurantUpserted`, `RestaurantStatusChanged` → upsert RESTAURANT docs |
| Kafka  | `menu.events` (consume)       | `MenuItemUpserted` → upsert MENU_ITEM docs                        |
| Feign  | `restaurant-service` → `/internal/restaurants/{id}` | Backfill/reconcile a restaurant doc (`clients.restaurant.url`) |
| Feign  | `menu-service` → `/internal/menu-items/{id}`        | Backfill/reconcile a menu-item doc (`clients.menu.url`)        |
| DB     | Postgres `search_db`          | Stores the `search_doc` read model                               |

Feign clients (`restaurant-service`, `menu-service`) each have a Resilience4j circuit breaker +
fallback and only ever call the dependency's `/internal/**` endpoints (PLATFORM_SPEC §1.1, §2.1).

### Read model

`SearchDoc { id, type(RESTAURANT|MENU_ITEM), refId, restaurantId, name, cuisine, priceCents, lat,
lng, available, updatedAt }`. Natural key is `(type, refId)`; `id` is a deterministic surrogate so
event redeliveries upsert in place (idempotent).

## Endpoints

### Public (via gateway at `http://localhost:8080/api/search`, direct at `http://localhost:8084`)

| Method | Path                      | Description                                              |
|--------|---------------------------|----------------------------------------------------------|
| GET    | `/api/search`             | Search restaurants + menu items by `q`, `cuisine`, `lat`, `lng` |
| GET    | `/api/search/restaurants` | Restaurants only                                         |
| GET    | `/api/search/menu-items`  | Menu items only                                          |

Query params (all optional): `q` (free text over name/cuisine), `cuisine` (exact), `lat`, `lng`
(proximity ranking via haversine distance).

### Internal (never exposed through the gateway)

| Method | Path                                      | Description                                  |
|--------|-------------------------------------------|----------------------------------------------|
| GET    | `/internal/search`                        | Service-to-service read-model lookup (`q`, `cuisine`) |
| POST   | `/internal/search/reconcile/menu-items/{id}` | Repair the index for a menu item from its authoritative source |

### Ops

| Path                  | Description            |
|-----------------------|------------------------|
| `/actuator/health`    | Health probe           |
| `/swagger-ui.html`    | Swagger UI             |
| `/v3/api-docs`        | OpenAPI JSON           |

## Run

```bash
# Requires a local Postgres (search_db) and Kafka (localhost:9092).
mvn spring-boot:run
# overridable env vars: SERVER_PORT, DB_URL, DB_USERNAME, DB_PASSWORD,
#                       KAFKA_BOOTSTRAP_SERVERS, KAFKA_GROUP_ID,
#                       RESTAURANT_URL, MENU_URL
```

Example query:

```bash
curl "http://localhost:8084/api/search?q=pizza&cuisine=ITALIAN&lat=51.51&lng=-0.13"
```

## Build

```bash
mvn -B verify          # compile, run tests (H2 + embedded Kafka), package
docker build -t quickbite/search-service:local .
```

## Deploy

Terraform lives in `terraform/`. It reads `platform-infra` remote state, provisions `search_db`
via the `postgres-db` module, and deploys onto the shared ECS/ALB via the `java-service` module
with `path_prefix = /api/search`.

```bash
cd terraform
terraform init \
  -backend-config="bucket=quickbite-tfstate-dev" \
  -backend-config="key=search-service/dev/terraform.tfstate" \
  -backend-config="region=us-east-1" \
  -backend-config="dynamodb_table=quickbite-tflocks"
terraform apply -var="image_tag=$(git rev-parse --short HEAD)"
```
