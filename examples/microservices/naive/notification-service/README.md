# notification-service

Notification fan-out for the QuickBite platform.

## What & why

`notification-service` keeps customers informed at every step of an order's lifecycle. It is a
**pure event consumer** (PLATFORM_SPEC §1.1, §3): it subscribes to the platform's domain event
streams and, for each relevant event, **renders a template**, **persists** a notification record
and **(mock) sends** it (the send is logged — a real implementation would call SES / Twilio / FCM).

It owns no write API; the only public endpoint is a read of a user's notification history. It has
**no synchronous dependencies** — per PLATFORM_SPEC §1.1 its links to order/delivery/payment are
entirely via Kafka events, so it holds no Feign clients. The recipient user id arrives on each
inbound event payload.

- **Port**: `8093`
- **Database**: `notification_db` (Postgres + Flyway)
- **Package**: `com.quickbite.notification`
- **Gateway path**: `/api/notifications/**`
- **Event role**: **consumer** of `orders.events`, `payments.events`, `deliveries.events`

## Dependencies

| Kind   | What                              | Notes                                                                 |
|--------|-----------------------------------|-----------------------------------------------------------------------|
| Kafka  | `orders.events` (consume)         | `OrderCreated`, `OrderStatusChanged` → render + send                  |
| Kafka  | `payments.events` (consume)       | `PaymentCaptured`, `PaymentFailed`, `Refunded` → render + send        |
| Kafka  | `deliveries.events` (consume)     | `DeliveryStatusChanged` → render + send                              |
| DB     | Postgres `notification_db`        | Stores the `notification` audit log                                  |

This service makes no synchronous cross-service calls (no Feign clients) — all upstream coupling is
through Kafka events (PLATFORM_SPEC §1.1, §5).

### Domain

`Notification { id, userId, channel(EMAIL|SMS|PUSH), template, payloadJson, status(PENDING|SENT|FAILED),
createdAt, sentAt }`. The example always uses the `EMAIL` channel and renders a short human-readable
message into `payloadJson`. Events are processed resiliently: an unparseable record is logged and
skipped rather than stalling the partition (PLATFORM_SPEC §5).

## Endpoints

### Public (through the gateway at `/api`)

| Method | Path                              | Description                              |
|--------|-----------------------------------|------------------------------------------|
| GET    | `/api/notifications?userId={id}`  | List a user's notifications, newest first |

### Internal (service-to-service, never gateway-exposed)

| Method | Path                                  | Description                                  |
|--------|---------------------------------------|----------------------------------------------|
| GET    | `/internal/notifications/users/{userId}` | List notifications delivered to a user    |

### Operational

| Method | Path                    | Description            |
|--------|-------------------------|------------------------|
| GET    | `/actuator/health`      | Liveness/readiness     |
| GET    | `/swagger-ui.html`      | Swagger UI             |
| GET    | `/v3/api-docs`          | OpenAPI JSON           |

The committed contract lives at [`api/openapi.yaml`](api/openapi.yaml).

## Configuration

All values are overridable via environment variables (see `src/main/resources/application.yml`):

| Env var                   | Default                                          | Purpose                          |
|---------------------------|--------------------------------------------------|----------------------------------|
| `SERVER_PORT`             | `8093`                                           | HTTP port                        |
| `DB_URL`                  | `jdbc:postgresql://localhost:5432/notification_db` | JDBC URL                       |
| `DB_USERNAME`             | `quickbite`                                       | DB user                          |
| `DB_PASSWORD`             | `quickbite`                                       | DB password                      |
| `KAFKA_BOOTSTRAP_SERVERS` | `localhost:9092`                                  | Kafka brokers                    |
| `KAFKA_GROUP_ID`          | `notification-service`                            | Consumer group                   |

## Run

```bash
# 1. Start Postgres + Kafka (from the repo root)
docker compose up -d postgres kafka

# 2. Run the service
mvn spring-boot:run
```

Swagger UI: http://localhost:8093/swagger-ui.html

## Build

```bash
mvn -B verify            # compile + run tests (H2 + embedded Kafka)
docker build -t quickbite/notification-service:local .
```

## Deploy

Terraform lives in [`terraform/`](terraform/). It reads `platform-infra` remote state, provisions a
dedicated `notification_db` via the `postgres-db` module, and deploys the container via the
`java-service` module with an ALB listener rule on `path_prefix = /api/notifications`.

```bash
cd terraform
terraform init \
  -backend-config="bucket=quickbite-tfstate-dev" \
  -backend-config="key=notification-service/dev/terraform.tfstate" \
  -backend-config="region=us-east-1" \
  -backend-config="dynamodb_table=quickbite-tflocks"
terraform apply -var="image_tag=$(git rev-parse --short HEAD)"
```
