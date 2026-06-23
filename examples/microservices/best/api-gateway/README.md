# api-gateway

The single **public edge** for the QuickBite platform. A reactive **Spring Cloud Gateway**
application (the only WebFlux app in the platform) that:

- routes all external `/api/**` traffic to the 14 backend services (see PLATFORM_SPEC §4);
- validates the **shared HS256 JWT** for protected routes and forwards `X-User-Id` /
  `X-User-Role` headers downstream;
- exposes CORS for the shell + every micro-frontend dev origin (`localhost:3000`–`3006`);
- **never** routes to any `/internal/**` endpoint.

It is fully **stateless** — no Postgres, no JPA, no Kafka.

- **Port:** `8080`
- **Base package:** `com.quickbite.gateway`
- **Public path prefix (ALB / gateway):** `/api`

## Why

Each backend service owns its own `/api/<resource>` path and its own `/internal/**` space.
Centralising auth and routing at one edge keeps the services free of cross-cutting concerns: they
trust the `X-User-Id` / `X-User-Role` headers the gateway injects after verifying the token, and
their internal endpoints are unreachable from the public internet.

## Dependencies

Runtime (Java / Spring):

- Java 21, Spring Boot 3.3.2, Spring Cloud 2023.0.3 (BOM-managed).
- `spring-cloud-starter-gateway` (reactive routing).
- `spring-boot-starter-actuator` (health + `gateway` actuator endpoint).
- `io.jsonwebtoken:jjwt` 0.12.6 (`-api` / `-impl` / `-jackson`) for edge JWT validation.
- `springdoc-openapi-starter-webflux-ui` 2.6.0 (Swagger UI).
- Lombok.

Routing targets (no Feign — the gateway proxies, it does not call): every backend service, by its
`clients.<service>.url` property (defaults to the local direct port, overridable per environment):

| Service              | Port | `clients.*.url` env override   |
|----------------------|------|--------------------------------|
| identity-service     | 8081 | `CLIENTS_IDENTITY_URL`         |
| restaurant-service   | 8082 | `CLIENTS_RESTAURANT_URL`       |
| menu-service         | 8083 | `CLIENTS_MENU_URL`             |
| search-service       | 8084 | `CLIENTS_SEARCH_URL`           |
| cart-service         | 8085 | `CLIENTS_CART_URL`             |
| pricing-service      | 8086 | `CLIENTS_PRICING_URL`          |
| promotion-service    | 8087 | `CLIENTS_PROMOTION_URL`        |
| order-service        | 8088 | `CLIENTS_ORDER_URL`            |
| payment-service      | 8089 | `CLIENTS_PAYMENT_URL`          |
| wallet-service       | 8090 | `CLIENTS_WALLET_URL`           |
| driver-service       | 8091 | `CLIENTS_DRIVER_URL`           |
| delivery-service     | 8092 | `CLIENTS_DELIVERY_URL`         |
| notification-service | 8093 | `CLIENTS_NOTIFICATION_URL`     |
| review-service       | 8094 | `CLIENTS_REVIEW_URL`           |

## Routing & auth rules

Every backend service serves its own `/api/<resource>/**` path, so the gateway forwards the path
unchanged (no `StripPrefix` / `RewritePath` needed — the target path equals the inbound path).

| Inbound path prefix                 | → service            |
|-------------------------------------|----------------------|
| `/api/auth/**`, `/api/users/**`     | identity-service     |
| `/api/restaurants/**`               | restaurant-service   |
| `/api/menu/**`                      | menu-service         |
| `/api/search/**`                    | search-service       |
| `/api/carts/**`                     | cart-service         |
| `/api/promotions/**`                | promotion-service    |
| `/api/pricing/**`                   | pricing-service      |
| `/api/orders/**`                    | order-service        |
| `/api/payments/**`                  | payment-service      |
| `/api/wallets/**`                   | wallet-service       |
| `/api/drivers/**`                   | driver-service       |
| `/api/deliveries/**`                | delivery-service     |
| `/api/notifications/**`             | notification-service |
| `/api/reviews/**`                   | review-service       |

The global `JwtAuthenticationFilter` (a `GlobalFilter`, order `-100`):

- **Anonymous** (no token required): `/api/auth/**`, `/api/search/**`, and **GET** on
  `/api/restaurants/**` + `/api/menu/**` (plus the gateway's own `/api/gateway/**`).
- **Protected** (everything else): requires `Authorization: Bearer <jwt>`; the token signature,
  issuer (`quickbite-identity`) and expiry are verified with the shared secret. On success the
  gateway strips any client-supplied `X-User-Id` / `X-User-Role` headers and sets its own from the
  token `sub` / `role` claims.
- `/internal/**` is rejected with **404** — it can never be reached through the edge.

Auth failures return the platform-standard envelope `{timestamp,status,error,message,path}`.

## Endpoints (served by the gateway itself)

| Method | Path                  | Auth       | Description                                  |
|--------|-----------------------|------------|----------------------------------------------|
| GET    | `/api/gateway/info`   | anonymous  | Configured route prefixes + public paths     |
| GET    | `/internal/routes`    | internal   | Full route table (id, prefix, target uri); not reachable through the edge |
| GET    | `/actuator/health`    | anonymous  | Liveness / readiness probe                    |
| GET    | `/swagger-ui.html`    | anonymous  | Swagger UI                                     |
| GET    | `/v3/api-docs`        | anonymous  | OpenAPI document                               |

All `/api/<resource>/**` paths above are **proxied** to the backend services (not served locally).

## Configuration (env overrides)

| Env var                 | Default                                              | Meaning                       |
|-------------------------|------------------------------------------------------|-------------------------------|
| `SERVER_PORT`           | `8080`                                               | Listen port                   |
| `JWT_SECRET`            | dev placeholder                                      | Shared HS256 signing secret   |
| `JWT_ISSUER`            | `quickbite-identity`                                 | Expected token issuer         |
| `CLIENTS_<SVC>_URL`     | `http://localhost:<port>`                            | Backend route target          |

## Build

```bash
mvn -B verify          # compile + run tests
mvn -B package         # build the executable jar (target/api-gateway-1.0.0.jar)
```

## Run

```bash
# locally (expects the backend services running on their local ports)
mvn spring-boot:run

# or the packaged jar
java -jar target/api-gateway-1.0.0.jar

# all defaults can be overridden, e.g.
JWT_SECRET=... CLIENTS_ORDER_URL=http://order-service:8088 java -jar target/api-gateway-1.0.0.jar
```

The full local stack (gateway + every service + Postgres + Kafka) runs via the repo-root
`docker-compose.yml`.

## Docker

```bash
docker build -t api-gateway:local .
docker run -p 8080:8080 -e JWT_SECRET=... api-gateway:local
```

## Deploy (Terraform)

```bash
cd terraform
terraform init \
  -backend-config="bucket=quickbite-tfstate-dev" \
  -backend-config="key=api-gateway/dev/terraform.tfstate" \
  -backend-config="region=us-east-1" \
  -backend-config="dynamodb_table=quickbite-tflocks"
terraform apply -var="image_tag=$(git rev-parse --short HEAD)" -var="jwt_secret=$JWT_SECRET"
```

The Terraform reads `platform-infra` remote state and deploys the gateway via the shared
`java-service` module with `path_prefix = "/api"` (the public edge) and `health_path =
/actuator/health`. It is stateless, so there is **no** `postgres-db` module.
