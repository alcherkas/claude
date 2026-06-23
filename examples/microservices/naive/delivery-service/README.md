# delivery-service

The **last-mile dispatch hub** of the QuickBite food-delivery platform. It turns a ready order
into a delivery, assigns an available courier, tracks the courier's location, and is the producer
of `deliveries.events`. Together with `order-service` it is one of the platform's high-fan-in hubs.

- **Port:** `8092`
- **Database:** `delivery_db` (Postgres + Flyway)
- **Base package:** `com.quickbite.delivery`
- **Gateway route:** `/api/deliveries/**` → delivery-service

## What it does

- **Create** a delivery for an order: read the order from order-service `/internal`, find an
  `AVAILABLE` driver and assign it via driver-service `/internal`, persist the delivery as
  `ASSIGNED`, then emit `DeliveryStatusChanged`.
- **Auto-create** a `PENDING` delivery when an order becomes `READY` (consumed from
  `orders.events`). This path is idempotent — a redelivered event will not duplicate a delivery.
- **Read** a delivery by id or by `orderId`, and expose its courier **tracking** trail.
- **Update** delivery status publicly; every transition emits `DeliveryStatusChanged`.

### Delivery creation flow (`POST /api/deliveries`)

1. `OrderClient` reads the order (`GET /internal/orders/{id}`).
2. `DriverClient` finds an available driver (`GET /internal/drivers/available`).
3. `DriverClient` assigns it (`POST /internal/drivers/{id}/assign`).
4. Delivery persisted with status `ASSIGNED`.
5. `DeliveryStatusChanged` emitted on `deliveries.events`.

## Domain model

- `Delivery{id, orderId, driverId, status, createdAt, trackingPoints[]}`
- `TrackingPoint{id, deliveryId, lat, lng, at}`
- `DeliveryStatus`: `PENDING, ASSIGNED, EN_ROUTE_TO_PICKUP, PICKED_UP, EN_ROUTE_TO_CUSTOMER,
  DELIVERED, FAILED`

## Dependencies

| Dependency      | Client         | Target endpoint(s)                                                   | URL property         |
|-----------------|----------------|---------------------------------------------------------------------|----------------------|
| order-service   | `OrderClient`  | `GET /internal/orders/{id}`                                         | `clients.order.url`  |
| driver-service  | `DriverClient` | `GET /internal/drivers/available`, `POST /internal/drivers/{id}/assign` | `clients.driver.url` |

Every Feign client has a Resilience4j circuit breaker and a fallback that surfaces a
`503 Service Unavailable` rather than half-creating a delivery.

### Eventing (Kafka)

| Topic               | Role     | Events                                                            |
|---------------------|----------|-------------------------------------------------------------------|
| `deliveries.events` | producer | `DeliveryStatusChanged`                                           |
| `orders.events`     | consumer | `OrderStatusChanged` → `READY` triggers an auto `PENDING` delivery |

Events use the envelope `{eventId,type,occurredAt,payload}`. Consumers log-and-skip on errors.

## Endpoints

### Public (via gateway `/api/deliveries`)

| Method | Path                              | Description                                         |
|--------|-----------------------------------|-----------------------------------------------------|
| POST   | `/api/deliveries`                 | Create a delivery `{orderId}` and assign a driver   |
| GET    | `/api/deliveries/{id}`            | Get a delivery by id                                |
| GET    | `/api/deliveries?orderId=`        | List deliveries for an order                        |
| PATCH  | `/api/deliveries/{id}/status`     | Update status `{status}` (emits event)              |
| GET    | `/api/deliveries/{id}/tracking`   | Courier tracking trail (oldest first)               |

### Internal (never exposed through the gateway)

| Method | Path                                | Returns                                              |
|--------|-------------------------------------|------------------------------------------------------|
| GET    | `/internal/deliveries/{id}`         | Delivery by id                                       |
| GET    | `/internal/deliveries?orderId=`     | The delivery for an order                            |

### Ops

- Health: `GET /actuator/health`
- OpenAPI: `GET /v3/api-docs`, Swagger UI: `/swagger-ui.html`, committed contract: `api/openapi.yaml`

## Run & build

```bash
# Build + test
mvn -B verify

# Run locally (defaults to localhost dependencies + Postgres on 5432/delivery_db, Kafka on 9092)
mvn spring-boot:run

# Build the container image
docker build -t quickbite/delivery-service:local .
docker run -p 8092:8092 quickbite/delivery-service:local
```

Key environment overrides (all have local defaults): `SERVER_PORT`, `DB_URL`, `DB_USER`,
`DB_PASSWORD`, `KAFKA_BOOTSTRAP_SERVERS`, `CLIENTS_ORDER_URL`, `CLIENTS_DRIVER_URL`,
`KAFKA_TOPIC_DELIVERIES_EVENTS`, `KAFKA_TOPIC_ORDERS_EVENTS`.

## Deploy (Terraform)

```bash
cd terraform
terraform init \
  -backend-config="bucket=quickbite-tfstate-dev" \
  -backend-config="dynamodb_table=quickbite-tflocks" \
  -backend-config="region=us-east-1"
terraform apply -var="image_tag=$(git rev-parse --short HEAD)"
```

Terraform reads `platform-infra` remote state and consumes the reusable `java-service` and
`postgres-db` modules from `terraform-modules`. The service is published behind the shared ALB at
`path_prefix = /deliveries`.

## Stack

Java 21 · Spring Boot 3.3.2 · Spring Cloud 2023.0.3 · Spring Data JPA + Flyway + Postgres ·
OpenFeign + Resilience4j · Spring Kafka · springdoc-openapi 2.6.0 · Lombok.
