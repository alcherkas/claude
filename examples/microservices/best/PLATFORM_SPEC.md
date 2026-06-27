# QuickBite — Platform Specification (canonical contract)

> This file is the **single source of truth** for the QuickBite multi-repo example.
> Every repository is generated to conform to the conventions, ports, package names,
> dependency wiring and endpoint contracts defined here. If anything is ambiguous in a
> repo, this document wins.

QuickBite is a **food-delivery marketplace** (think Deliveroo / DoorDash). Customers
discover restaurants, build a cart, check out, pay, and track a courier in real time.
Restaurant owners manage menus and incoming orders; couriers manage deliveries.

The platform is deliberately split into **independent repositories** (multi-repo /
"polyrepo"), one per deployable unit. Each repo is independently buildable and
deployable and owns its own Terraform.

---

## 1. Repository inventory

### 1.1 Backend services (Java 21 / Spring Boot) — each repo = Java API + Terraform

| # | Repo / service        | Port | Database (Postgres) | Event role        | Depends on (sync calls)                          |
|---|-----------------------|------|---------------------|-------------------|--------------------------------------------------|
| 1 | `identity-service`    | 8081 | `identity_db`       | —                 | — (foundational)                                 |
| 2 | `restaurant-service`  | 8082 | `restaurant_db`     | producer          | identity                                         |
| 3 | `menu-service`        | 8083 | `menu_db`           | producer          | restaurant                                       |
| 4 | `search-service`      | 8084 | `search_db`         | consumer          | restaurant, menu                                 |
| 5 | `cart-service`        | 8085 | `cart_db`           | —                 | menu, identity                                   |
| 6 | `promotion-service`   | 8087 | `promotion_db`      | —                 | identity                                         |
| 7 | `pricing-service`     | 8086 | _stateless_         | —                 | menu, promotion                                  |
| 8 | `order-service`       | 8088 | `order_db`          | **producer**      | cart, pricing, menu, restaurant, identity        |
| 9 | `wallet-service`      | 8090 | `wallet_db`         | —                 | identity                                         |
| 10| `payment-service`     | 8089 | `payment_db`        | producer          | order, wallet, identity                          |
| 11| `driver-service`      | 8091 | `driver_db`         | —                 | identity                                         |
| 12| `delivery-service`    | 8092 | `delivery_db`       | **producer**      | order, driver                                    |
| 13| `notification-service`| 8093 | `notification_db`   | **consumer**      | order, delivery, payment (via events)            |
| 14| `review-service`      | 8094 | `review_db`         | —                 | order, restaurant, identity                      |
| 15| `api-gateway`         | 8080 | _stateless_         | —                 | routes to all of the above                       |

> 14 business APIs + 1 gateway. "Foundational" services (identity) have no dependencies;
> `order-service` and `delivery-service` are the high-fan-in hubs.

### 1.2 Frontend micro-frontends (React + Vite + Module Federation) — each repo = React MFE + Terraform

| # | Repo / MFE              | Dev port | Federation name      | Talks to (via gateway)                         |
|---|-------------------------|----------|----------------------|------------------------------------------------|
| 1 | `shell` (host)          | 3000     | `shell`              | gateway (auth), loads all remotes              |
| 2 | `discovery-mfe`         | 3001     | `discovery`          | search, restaurant, menu                       |
| 3 | `checkout-mfe`          | 3002     | `checkout`           | cart, pricing, promotion, order, payment, wallet|
| 4 | `order-tracking-mfe`    | 3003     | `orderTracking`      | order, delivery                                |
| 5 | `account-mfe`           | 3004     | `account`            | identity, wallet, review, order                |
| 6 | `restaurant-admin-mfe`  | 3005     | `restaurantAdmin`    | restaurant, menu, order, promotion             |
| 7 | `driver-portal-mfe`     | 3006     | `driverPortal`       | driver, delivery                               |

### 1.3 Infrastructure repos (Terraform only)

| Repo                 | Purpose                                                                         |
|----------------------|---------------------------------------------------------------------------------|
| `platform-infra`     | Shared AWS foundation: VPC, subnets, ECS cluster, ALB, Route53, ECR, shared RDS, MSK/Redis. Exposes remote-state outputs that every service repo reads. |
| `terraform-modules`  | Reusable modules consumed by every repo: `java-service`, `react-mfe`, `postgres-db`, `redis-cache`. |

### 1.4 Root meta files (this directory)

`README.md`, `PLATFORM_SPEC.md` (this file), `ARCHITECTURE.md`, `docs/dependency-graph.md`,
`docker-compose.yml` (full local stack), `Makefile`, `.gitignore`.

---

## 2. Global conventions

### 2.1 Java service stack (pin exactly)
- **Java 21**, **Spring Boot 3.3.2**, **Spring Cloud 2023.0.3**, **Maven**.
- Starters: `spring-boot-starter-web`, `-validation`, `-actuator`; `-data-jpa` + `postgresql` +
  `flyway-core` for stateful services; `spring-cloud-starter-openfeign` +
  `spring-cloud-starter-circuitbreaker-resilience4j` for services with dependencies;
  `spring-boot-starter-security` + `io.jsonwebtoken:jjwt` (0.12.x) for `identity-service` & `api-gateway`;
  `spring-kafka` for event producers/consumers; `springdoc-openapi-starter-webmvc-ui` (2.6.0) everywhere;
  `org.projectlombok:lombok`. Tests: `spring-boot-starter-test`.
- `api-gateway` uses `spring-cloud-starter-gateway` (reactive) — it is the only WebFlux app.
- **Group id**: `com.quickbite`. **Base package**: `com.quickbite.<service>` (service name without
  the `-service` suffix, e.g. `com.quickbite.order`). Artifact id: `<name>` (e.g. `order-service`).
- **Layered package layout** inside the base package:
  `web` (REST controllers), `service` (business logic), `domain` (JPA entities + enums),
  `repository` (Spring Data repos), `dto` (request/response records), `client` (Feign clients to
  *other* services), `config`, `event` (Kafka producers/consumers). Stateless services omit
  `domain`/`repository`.
- **Inter-service calls use Feign clients** named `<Target>Client`, pointing at the target service
  by its `application.yml` property `clients.<target>.url` (defaults to the gateway-less direct URL
  for local, overridable per environment). Every Feign client has a Resilience4j circuit breaker and
  a fallback. Cross-service calls only ever hit the target's `/internal/**` endpoints.
- **Health**: actuator at `/actuator/health`. **OpenAPI/Swagger** at `/swagger-ui.html`,
  spec served at `/v3/api-docs` and committed as `api/openapi.yaml`.
- **Error handling**: a `@RestControllerAdvice` returning RFC-7807-style `{timestamp,status,error,message,path}`.
- **Migrations**: Flyway, `src/main/resources/db/migration/V1__init.sql`.
- **Config**: `application.yml` with `server.port`, `spring.application.name=<name>`,
  datasource, `clients.*.url` for dependencies, `kafka.bootstrap-servers` where relevant.
  All values overridable by env vars (`${DB_URL:jdbc:postgresql://localhost:5432/<db>}` style).

### 2.2 Standard Java repo file tree
```
<service>/
  README.md                      # what it does, deps, endpoints, run/build/deploy
  pom.xml
  Dockerfile                     # multi-stage: maven build -> eclipse-temurin:21-jre
  .dockerignore
  .gitignore
  .github/workflows/ci.yml       # mvn verify + docker build
  api/openapi.yaml               # committed contract
  src/main/java/com/quickbite/<svc>/
      <Svc>Application.java
      config/...                 # SecurityConfig / OpenApiConfig / FeignConfig as needed
      web/...Controller.java     # public + a separate Internal...Controller for /internal/**
      service/...Service.java
      domain/...                 # entities + enums (stateful only)
      repository/...Repository.java
      dto/...                    # Java records
      client/...Client.java      # Feign clients for each dependency + fallbacks
      event/...                  # KafkaProducer / @KafkaListener (where relevant)
  src/main/resources/
      application.yml
      db/migration/V1__init.sql  # stateful only
  src/test/java/com/quickbite/<svc>/...ApplicationTests.java   # context load + one slice test
  terraform/
      versions.tf  providers.tf  backend.tf  variables.tf  main.tf  outputs.tf  terraform.tfvars
```

### 2.3 React MFE stack (pin exactly)
- **React 18.3**, **TypeScript 5.5**, **Vite 5.4**, **`@originjs/vite-plugin-federation` 1.3.x**,
  **React Router 6**, **TanStack Query 5** for data fetching, **Axios** API client.
- The `shell` is the **host**; the six feature MFEs are **remotes** exposing `./App` (and the host
  consumes them lazily via React.lazy + Suspense). Shared singletons: `react`, `react-dom`,
  `react-router-dom`.
- API base URL comes from `import.meta.env.VITE_GATEWAY_URL` (defaults to `http://localhost:8080`).
- Each MFE ships a `Dockerfile` (node build → nginx static) + `nginx.conf`, and Terraform that
  deploys it as an S3 + CloudFront static site (host) / remote bundle.

### 2.4 Standard MFE repo file tree
```
<mfe>/
  README.md
  package.json   tsconfig.json   tsconfig.node.json   vite.config.ts   index.html   .gitignore
  .github/workflows/ci.yml
  Dockerfile   nginx.conf   .dockerignore
  src/
    main.tsx          # mounts <App/> (host) — remotes also keep a standalone bootstrap
    bootstrap.tsx
    App.tsx
    api/client.ts     # axios instance + typed endpoint fns
    types.ts
    components/...     # feature components (see frontend table)
    components/<feature pages>
  terraform/
    versions.tf  providers.tf  backend.tf  variables.tf  main.tf  outputs.tf  terraform.tfvars
```

### 2.5 Terraform conventions (pin exactly)
- **Terraform >= 1.6**, **AWS provider ~> 5.40**, region `us-east-1`, default env `dev`.
- **Remote state**: S3 backend `quickbite-tfstate-<env>` + DynamoDB lock table `quickbite-tflocks`.
  Key: `<repo>/<env>/terraform.tfstate`. (`backend.tf` carries a partial config; real values come
  from `-backend-config`.)
- Every service/MFE repo **reads `platform-infra` outputs** via
  `data "terraform_remote_state" "platform"` (S3 backend, key `platform-infra/<env>/terraform.tfstate`).
- Every service/MFE repo **consumes the reusable modules** from `terraform-modules` via a relative
  source `../../terraform-modules/modules/<name>` (a comment notes that in a real polyrepo this is a
  pinned git source: `git::https://github.com/quickbite/terraform-modules.git//modules/<name>?ref=v1.0.0`).
- `java-service` module inputs: `name, image, container_port, cpu, memory, desired_count, vpc_id,
  private_subnet_ids, ecs_cluster_arn, alb_listener_arn, path_prefix, environment, health_path,
  db_secret_arn (optional)`. It creates: ECR repo, task definition, ECS Fargate service, security
  group, ALB target group + listener rule on `path_prefix`, CloudWatch log group, app-autoscaling.
- `react-mfe` module inputs: `name, environment, domain_name (optional), price_class`. It creates:
  S3 bucket (private), CloudFront distribution + OAC, optional ACM/Route53.
- `postgres-db` module: `name, engine_version (16.3), instance_class (db.t4g.micro), allocated_storage,
  vpc_id, subnet_ids, allowed_security_group_ids`. Creates subnet group, SG, RDS instance, secret in
  Secrets Manager. `redis-cache` module: ElastiCache Redis replication group.
- Service Terraform wiring: `path_prefix = "/<resource>"` matching the gateway route (see §4), and
  passes `environment` map containing the same `clients.*.url`, `DB_*`, and `KAFKA_*` values as
  `application.yml` (service-discovery URLs use internal ALB / Cloud Map DNS in real envs; for the
  example they use `http://<service>.quickbite.internal:<port>`).

### 2.6 Naming, ports, and URLs (do not deviate)
- Local backend base URL of a service: `http://localhost:<port>`. Through the gateway:
  `http://localhost:8080/api/<resource>` (see §4 route table).
- Internal (service-to-service) calls target `http://<service>:<port>/internal/...` in docker-compose
  (service name == compose service name == repo name) and `clients.<target>.url` in config.
- Kafka bootstrap: `localhost:9092` (host) / `kafka:9092` (compose). Topics in §5.

---

## 3. Domain model highlights (per service)

- **identity-service** — `User{id,email,passwordHash,fullName,role(CUSTOMER|RESTAURANT_OWNER|COURIER|ADMIN),createdAt}`.
  Issues JWTs (HS256, `clients`/gateway share the secret). Public: register/login/me; Internal: validate user + fetch role.
- **restaurant-service** — `Restaurant{id,ownerUserId,name,cuisine,address,geo(lat,lng),status(PENDING|ACTIVE|SUSPENDED),openingHours}`.
  Validates `ownerUserId` against identity `/internal`. Emits `restaurant.events` on create/status change.
- **menu-service** — `MenuItem{id,restaurantId,name,description,priceCents,currency,category,available}`.
  Validates restaurant via restaurant `/internal`. Emits `menu.events`.
- **search-service** — denormalized read model `SearchDoc{type,refId,restaurantId,name,cuisine,priceCents,geo,available}`.
  Consumes `restaurant.events` + `menu.events` to build the index; serves `/search`. (Uses Postgres
  full-text in the example; a comment notes OpenSearch in production.)
- **cart-service** — `Cart{userId,restaurantId,items[CartItem{menuItemId,qty,unitPriceCents,name}],updatedAt}`.
  Re-prices items against menu `/internal`. Checkout returns an immutable cart snapshot.
- **promotion-service** — `Promotion{code,type(PERCENT|FIXED|FREE_DELIVERY),value,minSubtotalCents,validFrom,validTo,maxRedemptions,perUserLimit,active}`.
  Validates a code; `/internal/promotions/apply` reserves a redemption.
- **pricing-service** — stateless. `POST /pricing/quote` takes `{userId,restaurantId,items[],promoCode?,tipCents?}`,
  fetches authoritative prices from menu `/internal`, applies promotion `/internal`, returns
  `{subtotalCents,deliveryFeeCents,serviceFeeCents,taxCents,discountCents,tipCents,totalCents,lineItems[]}`.
  `tipCents` is the optional courier tip (absolute cents, untaxed): echoed back and added to `totalCents` *after* tax.
- **order-service** — `Order{id,userId,restaurantId,status(CREATED|CONFIRMED|PREPARING|READY|PICKED_UP|DELIVERED|CANCELLED),
  items[],pricing(snapshot),createdAt}`. Creation flow: load cart snapshot → call pricing quote →
  validate restaurant/menu → persist → emit `OrderCreated` on `orders.events`. Internal endpoint
  exposes order summary + amount for payment/review/delivery.
- **wallet-service** — `Wallet{userId,balanceCents,currency}` + `WalletTxn`. Internal debit/credit.
- **payment-service** — `Payment{id,orderId,userId,amountCents,tipCents,method(CARD|WALLET),status(AUTHORIZED|CAPTURED|REFUNDED|FAILED),provider}`.
  Validates order amount via order `/internal`, optionally debits wallet, "captures" via a mock PSP,
  emits `PaymentCaptured` on `payments.events`. `amountCents` is the order total (tip included); `tipCents` is recorded for the receipt.
- **driver-service** — `Driver{id,userId,name,vehicle,status(OFFLINE|AVAILABLE|ON_DELIVERY),geo}` + location pings.
- **delivery-service** — `Delivery{id,orderId,driverId,status(PENDING|ASSIGNED|EN_ROUTE_TO_PICKUP|PICKED_UP|EN_ROUTE_TO_CUSTOMER|DELIVERED|FAILED),trackingPoints[]}`.
  Assigns an available driver (driver `/internal`), reads order (order `/internal`), emits
  `DeliveryStatusChanged` on `deliveries.events`.
- **notification-service** — `Notification{id,userId,channel(EMAIL|SMS|PUSH),template,payload,status,sentAt}`.
  `@KafkaListener` on `orders.events` + `payments.events` + `deliveries.events`; renders + "sends"
  (mock) and persists. Exposes `GET /notifications?userId=`.
- **review-service** — `Review{id,orderId,userId,restaurantId,driverId?,rating(1-5),comment,createdAt}`.
  Verifies the order is `DELIVERED` and belongs to the user via order `/internal`.

---

## 4. API Gateway route table (Spring Cloud Gateway)

All external traffic enters at `http://localhost:8080`. Routes (StripPrefix where noted) — also the
`path_prefix` used by each service's Terraform ALB listener rule:

| Path prefix (`/api/**`)      | → service            | Notes                              |
|------------------------------|----------------------|------------------------------------|
| `/api/auth/**`, `/api/users/**`   | identity-service     | JWT issued here                    |
| `/api/restaurants/**`        | restaurant-service   | (menu sub-paths proxied too)       |
| `/api/menu/**`               | menu-service         |                                    |
| `/api/search/**`             | search-service       |                                    |
| `/api/carts/**`              | cart-service         |                                    |
| `/api/promotions/**`         | promotion-service    |                                    |
| `/api/pricing/**`            | pricing-service      |                                    |
| `/api/orders/**`             | order-service        |                                    |
| `/api/payments/**`           | payment-service      |                                    |
| `/api/wallets/**`            | wallet-service       |                                    |
| `/api/drivers/**`            | driver-service       |                                    |
| `/api/deliveries/**`         | delivery-service     |                                    |
| `/api/notifications/**`      | notification-service |                                    |
| `/api/reviews/**`            | review-service       |                                    |

The gateway validates the JWT (shared secret) for non-public routes and forwards
`X-User-Id` / `X-User-Role` headers downstream. `/internal/**` is **never** exposed through the gateway.

---

## 5. Eventing (Kafka)

| Topic               | Produced by         | Consumed by                  | Key events                                  |
|---------------------|---------------------|------------------------------|---------------------------------------------|
| `orders.events`     | order-service       | notification, delivery, search (n/a) | `OrderCreated`, `OrderStatusChanged`        |
| `payments.events`   | payment-service     | notification, order          | `PaymentCaptured`, `PaymentFailed`, `Refunded` |
| `deliveries.events` | delivery-service    | notification, order          | `DeliveryStatusChanged`                     |
| `restaurant.events` | restaurant-service  | search                       | `RestaurantUpserted`, `RestaurantStatusChanged` |
| `menu.events`       | menu-service        | search                       | `MenuItemUpserted`                          |

Events are JSON with an envelope `{eventId,type,occurredAt,payload}`. Keep the producer/consumer
code thin and resilient (log + skip on deserialize errors).

---

## 6. End-to-end happy path (the demo narrative)

1. Customer registers/logs in (`identity`) → JWT.
2. Browses via `discovery-mfe` → `search`/`restaurant`/`menu`.
3. Adds items (`cart`, re-priced against `menu`).
4. Checkout (`checkout-mfe`): customer may add an optional courier **tip** → `pricing` quote
   (+ `promotion`, + `tipCents` in `totalCents`) → `order` created (snapshot incl. `tipCents`) →
   `payment` captured for the tip-inclusive total (optionally `wallet`) → `OrderCreated`/`PaymentCaptured` events.
5. `delivery` assigns a `driver`; `order-tracking-mfe` shows live status from `deliveries.events`.
6. `notification` emails the customer at each step.
7. After delivery, customer leaves a `review` (verified against `order`).

---

## 7. Generation rules for agents (must follow)

- Write **real, coherent, compilable-looking** code — not stubs or `// TODO` placeholders. It does
  not need to actually run, but every file must be internally consistent and reference the exact
  ports/paths/package names above.
- **Do NOT run** `mvn`, `npm install`, `terraform init`, or any build — only create files.
- Use the **exact** ports, DB names, package names, Feign client URLs, gateway paths, topic names,
  and module inputs from this spec. Cross-check dependencies in §1.1 before writing a Feign client.
- Keep each file focused; prefer clarity over volume. One representative test per service is enough.
- Every repo gets its own `README.md` and its own `terraform/` directory.
