# menu-service

QuickBite **menu-service** — owns restaurant menu items.

- **Port:** `8083`
- **Database:** `menu_db` (Postgres + Flyway)
- **Base package:** `com.quickbite.menu`
- **Gateway path:** `/api/menu/**` (via api-gateway on `:8080`)
- **Event role:** **producer** — emits `MenuItemUpserted` on the `menu.events` topic
- **Depends on:** `restaurant-service` (sync, Feign → `/internal/restaurants/{id}`)

## What & why

Restaurant owners manage the dishes that customers can order. This service is the
system of record for `MenuItem`s. Before an item is created it confirms (synchronously)
that the owning restaurant exists and is `ACTIVE` in restaurant-service. On every
create/availability change it publishes a `MenuItemUpserted` event so the
search-service read model stays in sync.

`MenuItem{id, restaurantId, name, description, priceCents, currency, category, available, createdAt}`

## Dependencies

| Kind | Target | How |
|------|--------|-----|
| Sync | restaurant-service | Feign `RestaurantClient` → `GET ${clients.restaurant.url}/internal/restaurants/{id}`, Resilience4j circuit breaker + fallback |
| Async (out) | search-service | Kafka producer → topic `menu.events`, event `MenuItemUpserted` |
| Datastore | Postgres `menu_db` | Spring Data JPA + Flyway |

### Stack

Java 21, Spring Boot 3.3.2, Spring Cloud 2023.0.3 (BOM), Maven, Lombok,
springdoc-openapi 2.6.0. Starters: web, validation, actuator, data-jpa, postgresql,
flyway, openfeign, circuitbreaker-resilience4j, spring-kafka.

## Endpoints

### Public (through the gateway at `/api/menu`)

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/api/menu` | Create a menu item. Body carries `restaurantId`; validates the restaurant exists and is `ACTIVE`. Emits `MenuItemUpserted`. → `201` |
| `GET`  | `/api/menu?restaurantId={uuid}` | List a restaurant's menu items (ordered by category, name). |
| `GET`  | `/api/menu/{id}` | Fetch a single menu item. `404` if missing. |
| `PATCH`| `/api/menu/{id}/availability` | Toggle availability (`{"available": true|false}`). Re-emits `MenuItemUpserted`. |

### Internal (service-to-service, never exposed through the gateway)

| Method | Path | Returns |
|--------|------|---------|
| `GET` | `/internal/menu-items/{id}` | `{id, restaurantId, name, priceCents, currency, available}` |

### Ops

- Health: `GET /actuator/health`
- OpenAPI JSON: `GET /v3/api-docs` · Swagger UI: `GET /swagger-ui.html`
- Committed contract: [`api/openapi.yaml`](api/openapi.yaml)

All errors use the envelope `{timestamp, status, error, message, path}`.

## Configuration

Every value is overridable via environment variables (see `src/main/resources/application.yml`):

| Env var | Default | Meaning |
|---------|---------|---------|
| `SERVER_PORT` | `8083` | HTTP port |
| `DB_URL` | `jdbc:postgresql://localhost:5432/menu_db` | JDBC URL |
| `DB_USERNAME` / `DB_PASSWORD` | `quickbite` / `quickbite` | DB credentials |
| `CLIENTS_RESTAURANT_URL` | `http://localhost:8082` | restaurant-service base URL |
| `KAFKA_BOOTSTRAP_SERVERS` | `localhost:9092` | Kafka brokers |
| `KAFKA_TOPIC_MENU_EVENTS` | `menu.events` | Output topic |

## Run, build, deploy

### Build & test

```bash
mvn -B verify
```

### Run locally

Requires Postgres (`menu_db`), Kafka, and restaurant-service reachable. Easiest via the
root `docker-compose.yml`:

```bash
docker compose up menu-service
```

Or directly:

```bash
DB_URL=jdbc:postgresql://localhost:5432/menu_db \
CLIENTS_RESTAURANT_URL=http://localhost:8082 \
KAFKA_BOOTSTRAP_SERVERS=localhost:9092 \
mvn spring-boot:run
```

Swagger UI: http://localhost:8083/swagger-ui.html

### Docker

```bash
docker build -t quickbite/menu-service:local .
docker run -p 8083:8083 \
  -e DB_URL=jdbc:postgresql://host.docker.internal:5432/menu_db \
  -e CLIENTS_RESTAURANT_URL=http://host.docker.internal:8082 \
  quickbite/menu-service:local
```

### Deploy (Terraform)

The `terraform/` dir reads `platform-infra` remote state and consumes the reusable
`java-service` + `postgres-db` modules from `terraform-modules`.

```bash
cd terraform
terraform init \
  -backend-config="bucket=quickbite-tfstate-dev" \
  -backend-config="dynamodb_table=quickbite-tflocks" \
  -backend-config="region=us-east-1"
terraform apply -var="image_tag=$(git rev-parse --short HEAD)"
```

This provisions an RDS Postgres (`menu_db`), an ECS Fargate service, and an ALB listener
rule on `path_prefix = /api/menu`.
