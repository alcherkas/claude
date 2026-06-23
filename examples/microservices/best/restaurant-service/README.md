# restaurant-service

QuickBite's restaurant catalog service. It owns the lifecycle of restaurants in
the marketplace: owners register a restaurant, customers discover restaurants by
cuisine/city, and owners (or admins) drive the status lifecycle
`PENDING → ACTIVE → SUSPENDED`.

- **Port:** `8082`
- **Database:** Postgres `restaurant_db` (Flyway-managed)
- **Base package:** `com.quickbite.restaurant`
- **Gateway path:** `/api/restaurants/**`
- **Event role:** producer on Kafka topic `restaurant.events`

## Why

It is the authoritative source of restaurant identity and location for the rest
of the platform. menu-service, search-service, order-service and review-service
read compact restaurant summaries from its `/internal` API, and search-service
keeps its read model in sync by consuming `restaurant.events`.

## Dependencies

| Kind        | Target            | How                                                                 |
|-------------|-------------------|---------------------------------------------------------------------|
| Sync (Feign)| identity-service  | `IdentityClient` → `clients.identity.url` `/internal/users/{id}/validate` to confirm the owner is a `RESTAURANT_OWNER`. Resilience4j circuit breaker + fail-closed fallback. |
| Async (Kafka)| `restaurant.events` | Producer. Emits `RestaurantUpserted` (on create) and `RestaurantStatusChanged` (on status change). |

Spring Boot 3.3.2 / Spring Cloud 2023.0.3 / Java 21. Starters: web, validation,
actuator, data-jpa (+ postgresql + flyway), openfeign,
circuitbreaker-resilience4j, kafka, springdoc-openapi 2.6.0, lombok.

## Domain

`Restaurant{ id, ownerUserId, name, cuisine, addressLine, city, lat, lng,
status(PENDING|ACTIVE|SUSPENDED), openingHours, createdAt }`.

## Endpoints

### Public (`/api/restaurants`, exposed through the gateway)

| Method | Path                          | Description |
|--------|-------------------------------|-------------|
| POST   | `/api/restaurants`            | Register a restaurant. Validates `ownerUserId` is a `RESTAURANT_OWNER` via identity-service, persists as `PENDING`, emits `RestaurantUpserted`. |
| GET    | `/api/restaurants`            | List restaurants. Optional `cuisine` and `city` query filters. |
| GET    | `/api/restaurants/{id}`       | Fetch one restaurant. |
| PATCH  | `/api/restaurants/{id}/status`| Change status. Emits `RestaurantStatusChanged`. |

### Internal (`/internal`, service-to-service only, never via the gateway)

| Method | Path                            | Returns |
|--------|---------------------------------|---------|
| GET    | `/internal/restaurants/{id}`    | `{ id, name, status, lat, lng, cuisine }` |

### Ops

- Health: `GET /actuator/health`
- OpenAPI UI: `/swagger-ui.html` · spec: `/v3/api-docs` (committed at `api/openapi.yaml`)

## Error format

All errors return `{ timestamp, status, error, message, path }` via a
`@RestControllerAdvice`. Unknown id → `404`; invalid owner → `422`; bean
validation failure → `400`.

## Run locally

Requires a Postgres reachable at `jdbc:postgresql://localhost:5432/restaurant_db`,
identity-service on `:8081`, and Kafka on `localhost:9092` (all overridable).

```bash
# from the repo root
mvn spring-boot:run
```

Override config with env vars (defaults shown):

| Env var                   | Default                                            |
|---------------------------|----------------------------------------------------|
| `SERVER_PORT`             | `8082`                                             |
| `DB_URL`                  | `jdbc:postgresql://localhost:5432/restaurant_db`   |
| `DB_USERNAME`             | `quickbite`                                         |
| `DB_PASSWORD`             | `quickbite`                                         |
| `CLIENTS_IDENTITY_URL`    | `http://localhost:8081`                            |
| `KAFKA_BOOTSTRAP_SERVERS` | `localhost:9092`                                   |

## Build

```bash
mvn -B verify            # compile, run tests, package
docker build -t restaurant-service:local .
```

## Deploy

Terraform under `terraform/` deploys the service as an ECS Fargate task behind
the shared ALB and provisions a dedicated `restaurant_db` Postgres instance. It
reads `platform-infra` remote state and consumes the reusable `java-service` and
`postgres-db` modules from `terraform-modules`.

```bash
cd terraform
terraform init \
  -backend-config="bucket=quickbite-tfstate-dev" \
  -backend-config="region=us-east-1" \
  -backend-config="dynamodb_table=quickbite-tflocks"
terraform apply -var="env=dev" -var="image_tag=$(git rev-parse --short HEAD)"
```

CI (`.github/workflows/ci.yml`) runs `mvn -B verify` on JDK 21 and builds the
Docker image on every push/PR to `main`.
