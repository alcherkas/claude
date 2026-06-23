# driver-service

Courier management for the **QuickBite** food-delivery marketplace. It tracks
registered couriers, their availability, and their live location, and provides
the nearest-available-driver lookup and assignment used by `delivery-service`
during dispatch.

- **Port:** `8091`
- **Database:** `driver_db` (Postgres + Flyway)
- **Base package:** `com.quickbite.driver`
- **Gateway prefix:** `/api/drivers`
- **Event role:** none
- **Depends on:** `identity-service` (validates the COURIER role)

## What & why

A user registered in `identity-service` with the `COURIER` role can create a
driver profile here. Couriers toggle availability (`OFFLINE`, `AVAILABLE`,
`ON_DELIVERY`) and push location pings. When an order is ready for dispatch,
`delivery-service` calls this service's `/internal` API to find the nearest
`AVAILABLE` driver and atomically transition them to `ON_DELIVERY`.

Driver registration is gated on identity-service: the `IdentityClient` Feign
call to `/internal/users/{id}/validate?role=COURIER` must return `valid=true`.
The client is guarded by a Resilience4j circuit breaker and a fail-closed
fallback, so registrations are rejected (never silently trusted) when identity
is unavailable.

## Dependencies (stack pinned)

- Java 21, Spring Boot 3.3.2, Spring Cloud 2023.0.3 (BOM), Maven
- `spring-boot-starter-web`, `-validation`, `-actuator`, `-data-jpa`
- `postgresql`, `flyway-core`, `flyway-database-postgresql`
- `spring-cloud-starter-openfeign`, `spring-cloud-starter-circuitbreaker-resilience4j`
- `springdoc-openapi-starter-webmvc-ui` 2.6.0, Lombok
- Tests: `spring-boot-starter-test`, H2

## Endpoints

### Public (via gateway at `/api/drivers`)

| Method | Path                              | Description                                           |
|--------|-----------------------------------|-------------------------------------------------------|
| POST   | `/api/drivers`                    | Register a courier (validates COURIER role)           |
| GET    | `/api/drivers/{id}`               | Fetch a driver                                        |
| PATCH  | `/api/drivers/{id}/availability`  | Update availability `{status}`                        |
| POST   | `/api/drivers/{id}/location`      | Record a location ping `{lat,lng}`                    |
| GET    | `/api/drivers/available?lat=&lng=`| List AVAILABLE drivers nearest to a point             |

### Internal (service-to-service, never via gateway)

| Method | Path                                   | Description                                  |
|--------|----------------------------------------|----------------------------------------------|
| GET    | `/internal/drivers/available?lat=&lng=`| Nearest single AVAILABLE driver               |
| POST   | `/internal/drivers/{id}/assign`        | Assign a driver → `ON_DELIVERY`               |

Consumed by `delivery-service`.

### Operational

- Health: `GET /actuator/health`
- OpenAPI UI: `GET /swagger-ui.html` · spec: `GET /v3/api-docs` (committed at `api/openapi.yaml`)

## Configuration

All values are overridable via environment variables (see `application.yml`):

| Env var        | Default                                      | Purpose                       |
|----------------|----------------------------------------------|-------------------------------|
| `SERVER_PORT`  | `8091`                                        | HTTP port                     |
| `DB_URL`       | `jdbc:postgresql://localhost:5432/driver_db`  | Datasource URL                |
| `DB_USER`      | `driver`                                      | Datasource user               |
| `DB_PASSWORD`  | `driver`                                      | Datasource password           |
| `IDENTITY_URL` | `http://localhost:8081`                       | identity-service base URL     |

## Run & build

```bash
# Run tests + package
mvn -B verify

# Run locally (needs Postgres on localhost:5432 with database driver_db)
mvn spring-boot:run

# Build the container image
docker build -t quickbite/driver-service:local .
docker run -p 8091:8091 \
  -e DB_URL=jdbc:postgresql://host.docker.internal:5432/driver_db \
  -e IDENTITY_URL=http://host.docker.internal:8081 \
  quickbite/driver-service:local
```

## Deploy

Terraform in `terraform/` deploys an ECS Fargate service behind the shared ALB
plus a dedicated `driver_db` RDS instance, reading shared foundation outputs from
`platform-infra` and consuming the reusable `java-service` and `postgres-db`
modules from `terraform-modules`.

```bash
cd terraform
terraform init \
  -backend-config="bucket=quickbite-tfstate-dev" \
  -backend-config="dynamodb_table=quickbite-tflocks" \
  -backend-config="region=us-east-1"
terraform apply -var="image_tag=$(git rev-parse --short HEAD)"
```

The ALB listener rule routes `path_prefix = "/drivers"`, matching the gateway
route table in `PLATFORM_SPEC.md` §4.
