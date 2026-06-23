# QuickBite — Architecture

This document is the narrative companion to [`PLATFORM_SPEC.md`](./PLATFORM_SPEC.md). The spec is
authoritative for exact ports, names and wiring; this document explains how the pieces fit together.

---

## 1. Layered overview

```
                         ┌──────────────────────────────┐
   Browsers / Couriers   │   CloudFront (MFE bundles)    │   shell + 6 remotes (Module Federation)
                         └───────────────┬──────────────┘
                                         │  VITE_GATEWAY_URL
                         ┌───────────────▼──────────────┐
   EDGE / GATEWAY        │   api-gateway  (:8080)        │   Spring Cloud Gateway (WebFlux)
                         │   JWT validation, routing     │   forwards X-User-Id / X-User-Role
                         └───────────────┬──────────────┘
                                         │  /api/** → /<service>
         ┌───────────────────────────────┼───────────────────────────────┐
         │                               │                               │
   ┌─────▼─────┐                  ┌──────▼──────┐                 ┌───────▼───────┐
   │ identity  │  restaurant menu │   order     │  pricing cart   │   delivery    │   BUSINESS
   │ search    │  promotion       │   payment   │  wallet         │   driver      │   SERVICES
   │ review    │  notification    │             │                 │               │   (Spring Boot)
   └─────┬─────┘                  └──────┬──────┘                 └───────┬───────┘
         │  JPA / Flyway                 │  JPA                          │  JPA
   ┌─────▼───────────────────────────────▼───────────────────────────────▼───────┐
   │                       Postgres (per-service *_db schemas)                    │   DATA
   │   identity_db restaurant_db menu_db search_db cart_db promotion_db ...       │
   └─────────────────────────────────────────────────────────────────────────────┘
                                         ▲
                                         │  produce / consume
   ┌─────────────────────────────────────┴───────────────────────────────────────┐
   │   Kafka topics: orders.events  payments.events  deliveries.events            │   EVENTING
   │                 restaurant.events  menu.events                               │
   └─────────────────────────────────────────────────────────────────────────────┘
```

The five layers:

1. **Edge / gateway** — `api-gateway` (:8080) is the only public entrypoint. It is the sole WebFlux
   app (Spring Cloud Gateway), validates JWTs and routes `/api/**` to business services.
2. **Business services** — 14 Spring Boot services (:8081–:8094), each owning one bounded context.
3. **Data** — one Postgres database per stateful service (`pricing-service` and `api-gateway` are
   stateless). Migrations via Flyway.
4. **Eventing** — Kafka carries domain events for asynchronous, decoupled fan-out.
5. **Frontends** — `shell` host plus six remote MFEs, federated at runtime, all calling the gateway.

---

## 2. Synchronous dependency wiring (Feign)

Cross-service calls are **Feign clients** named `<Target>Client`, each with a Resilience4j circuit
breaker + fallback, targeting `clients.<target>.url` and only ever the target's `/internal/**`
endpoints. The full dependency edges (from spec §1.1):

| Service               | Calls (sync, via Feign `/internal`)                       |
|-----------------------|-----------------------------------------------------------|
| `identity-service`    | — (foundational)                                          |
| `restaurant-service`  | identity                                                 |
| `menu-service`        | restaurant                                                |
| `search-service`      | restaurant, menu                                          |
| `cart-service`        | menu, identity                                            |
| `promotion-service`   | identity                                                 |
| `pricing-service`     | menu, promotion                                           |
| `order-service`       | cart, pricing, menu, restaurant, identity                |
| `wallet-service`      | identity                                                 |
| `payment-service`     | order, wallet, identity                                   |
| `driver-service`      | identity                                                 |
| `delivery-service`    | order, driver                                             |
| `notification-service`| order, delivery, payment (primarily via events)          |
| `review-service`      | order, restaurant, identity                               |

`identity-service` is the foundational leaf (no dependencies); `order-service` and
`delivery-service` are the high-fan-in hubs. The full graph is in
[`docs/dependency-graph.md`](./docs/dependency-graph.md).

---

## 3. Kafka topics — producers and consumers

| Topic               | Produced by         | Consumed by                          | Key events                                     |
|---------------------|---------------------|--------------------------------------|------------------------------------------------|
| `orders.events`     | order-service       | notification, delivery               | `OrderCreated`, `OrderStatusChanged`           |
| `payments.events`   | payment-service     | notification, order                  | `PaymentCaptured`, `PaymentFailed`, `Refunded` |
| `deliveries.events` | delivery-service    | notification, order                  | `DeliveryStatusChanged`                        |
| `restaurant.events` | restaurant-service  | search                               | `RestaurantUpserted`, `RestaurantStatusChanged`|
| `menu.events`       | menu-service        | search                               | `MenuItemUpserted`                             |

Events are JSON with an envelope `{eventId, type, occurredAt, payload}`. Producer/consumer code is
thin and resilient: log and skip on deserialize errors. `search-service` builds its read model
purely from `restaurant.events` + `menu.events`; `notification-service` reacts to the three
operational topics; `order-service` consumes `payments.events` + `deliveries.events` to advance
order status.

---

## 4. End-to-end happy path (spec §6)

```
Customer        identity     discovery   cart    pricing   order        payment      delivery   driver  notification
   │  register/login │           │         │        │        │            │             │         │          │
   ├────────────────►│  JWT      │         │        │        │            │             │         │          │
   │  browse         │           │         │        │        │            │             │         │          │
   ├────────────────────────────►│ search/restaurant/menu    │            │             │         │          │
   │  add items      │           │         │        │        │            │             │         │          │
   ├──────────────────────────────────────►│ re-price vs menu/internal    │             │         │          │
   │  checkout       │           │         │        │        │            │             │         │          │
   ├───────────────────────────────────────────────►│ quote (+promotion) │             │         │          │
   │                 │           │         │        ├───────►│ create order (snapshot)  │         │          │
   │                 │           │         │        │        ├═══ OrderCreated → orders.events ══════════════►│ email
   │                 │           │         │        │        │            │ capture     │         │          │
   │                 │           │         │        │        │◄───────────┤ (optional wallet debit)          │
   │                 │           │         │        │        │            ├═══ PaymentCaptured → payments.events ═══════►│ email
   │                 │           │         │        │        │            │             │ assign  │          │
   │                 │           │         │        │        │            │             ├────────►│ available driver
   │                 │           │         │        │        │            │             ├═══ DeliveryStatusChanged → deliveries.events ►│ email
   │  track          │           │         │        │        │ (order advances from delivery + payment events)         │
   │  review         │           │         │        │        │ verify DELIVERED via order/internal                    │
```

1. Customer registers/logs in (`identity`) → JWT.
2. Browses via `discovery-mfe` → `search` / `restaurant` / `menu`.
3. Adds items (`cart`, re-priced against `menu`).
4. Checkout (`checkout-mfe`): `pricing` quote (+ `promotion`) → `order` created (immutable snapshot)
   → `payment` captured (optionally `wallet`) → `OrderCreated` / `PaymentCaptured` events emitted.
5. `delivery` assigns an available `driver`; `order-tracking-mfe` shows live status driven by
   `deliveries.events`.
6. `notification` emails the customer at each step (consuming the three operational topics).
7. After delivery, the customer leaves a `review`, verified `DELIVERED` against `order/internal`.

---

## 5. Security

- **JWT (HS256)** is issued by `identity-service` on register/login. The signing secret
  (`JWT_SECRET`) is **shared** between `identity-service` and `api-gateway`.
- The **gateway validates** the JWT on every non-public route, then forwards `X-User-Id` and
  `X-User-Role` headers downstream. Business services trust these headers (they sit on a private
  network behind the ALB) rather than re-validating tokens.
- `/api/auth/**` and `/api/users/register|login` are public; everything else requires a valid token.
- `/internal/**` endpoints are **never** exposed through the gateway — they carry no JWT and are
  reachable only on the internal network (compose network locally, internal ALB / Cloud Map in AWS).

---

## 6. Deployment topology

- **Services** run as **ECS Fargate** tasks behind an **internal/edge ALB**. Each `java-service`
  Terraform module creates an ECR repo, task definition, Fargate service, security group, an ALB
  target group + listener rule on its `path_prefix` (matching the gateway route), a CloudWatch log
  group and app-autoscaling.
- **MFEs** are static bundles in **private S3** served via **CloudFront + OAC** (the `react-mfe`
  module), with optional ACM/Route53. The `shell` host lazy-loads the six remotes via Module
  Federation at runtime.
- **Data**: Postgres on **RDS** (`postgres-db` module — subnet group, SG, instance, Secrets
  Manager secret per `*_db`). Eventing on **MSK** (Kafka); optional **ElastiCache Redis**
  (`redis-cache` module).
- **Foundation**: `platform-infra` provisions the VPC, public/private subnets, ECS cluster, ALB,
  Route53 zone, ECR registry and shared RDS/MSK, exporting remote-state outputs that every service
  and MFE repo reads via `data "terraform_remote_state" "platform"`.

---

## 7. Per-environment notes

- **Default region** `us-east-1`; default environment `dev`. Other envs (`staging`, `prod`) reuse
  the same modules with different `environment` and sizing inputs.
- **Remote state**: S3 bucket `quickbite-tfstate-<env>` + DynamoDB lock table `quickbite-tflocks`,
  key `<repo>/<env>/terraform.tfstate`. `backend.tf` carries a partial config; real values come via
  `-backend-config`.
- **Service discovery URLs** differ per env: `http://localhost:<port>` (host),
  `http://<service>:<port>` (compose), `http://<service>.quickbite.internal:<port>` (AWS internal
  ALB / Cloud Map). These map onto the `clients.<target>.url` config property.
- **Sizing** scales by env: `db.t4g.micro` and small Fargate tasks in `dev`; larger instance
  classes, higher `desired_count` and autoscaling ceilings in `prod`. CloudFront `price_class`
  likewise narrows in `dev` and widens in `prod`.
