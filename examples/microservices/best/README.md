# QuickBite — Multi-Repo Platform

QuickBite is a **food-delivery marketplace** (think Deliveroo / DoorDash). Customers discover
restaurants, build a cart, check out, pay, and track a courier in real time. Restaurant owners
manage menus and incoming orders; couriers manage deliveries.

The platform is deliberately split into **independent repositories** (multi-repo / "polyrepo"),
one per deployable unit. Each repo is independently buildable and deployable and owns its own
Terraform. **`PLATFORM_SPEC.md` is the canonical contract** — every port, database name, package
name, gateway route, Feign URL, Kafka topic and Terraform input is defined there and wins over
any individual repo.

---

## Repository inventory

### Backend services (Java 21 / Spring Boot 3.3.2)

| Repo / service          | Port | Purpose                                                                 |
|-------------------------|------|-------------------------------------------------------------------------|
| `identity-service`      | 8081 | Users, registration/login, JWT issuance, role lookup (foundational).    |
| `restaurant-service`    | 8082 | Restaurant profiles, status, geo; emits `restaurant.events`.            |
| `menu-service`          | 8083 | Menu items per restaurant; emits `menu.events`.                         |
| `search-service`        | 8084 | Denormalized search index; consumes restaurant + menu events.          |
| `cart-service`          | 8085 | Per-user carts, re-priced against the menu.                            |
| `pricing-service`       | 8086 | Stateless price quote: subtotal, fees, tax, promo discount, total.     |
| `promotion-service`     | 8087 | Promo codes, validation and redemption reservation.                   |
| `order-service`         | 8088 | Order lifecycle; emits `orders.events` (high fan-in hub).             |
| `payment-service`       | 8089 | Mock PSP capture, optional wallet debit; emits `payments.events`.     |
| `wallet-service`        | 8090 | Customer wallet balance + transactions.                               |
| `driver-service`        | 8091 | Couriers, status and location pings.                                  |
| `delivery-service`      | 8092 | Delivery assignment + tracking; emits `deliveries.events`.            |
| `notification-service`  | 8093 | Consumes order/payment/delivery events; renders + "sends" messages.    |
| `review-service`        | 8094 | Post-delivery reviews, verified against the order.                    |
| `api-gateway`           | 8080 | Single edge: routing, JWT validation, header propagation.             |

### Frontend micro-frontends (React 18 + Vite + Module Federation)

| Repo / MFE              | Port | Purpose                                                                 |
|-------------------------|------|-------------------------------------------------------------------------|
| `shell`                 | 3000 | Host application; auth shell; lazy-loads all remotes.                  |
| `discovery-mfe`         | 3001 | Browse / search restaurants and menus.                                |
| `checkout-mfe`          | 3002 | Cart, pricing, promotions, order placement and payment.               |
| `order-tracking-mfe`    | 3003 | Live order + delivery tracking.                                       |
| `account-mfe`           | 3004 | Profile, wallet, past orders, reviews.                                |
| `restaurant-admin-mfe`  | 3005 | Restaurant owner console: menu, incoming orders, promotions.          |
| `driver-portal-mfe`     | 3006 | Courier console: availability and active deliveries.                  |

### Infrastructure repos (Terraform only)

| Repo                 | Purpose                                                                          |
|----------------------|----------------------------------------------------------------------------------|
| `platform-infra`     | Shared AWS foundation: VPC, subnets, ECS cluster, ALB, Route53, ECR, RDS, MSK/Redis. Exposes remote-state outputs every repo reads. |
| `terraform-modules`  | Reusable modules: `java-service`, `react-mfe`, `postgres-db`, `redis-cache`.     |

**Total: 15 Java services + 7 MFEs + `platform-infra` + `terraform-modules`.**

---

## Multi-repo philosophy

- **One repo = one deployable unit.** Each service / MFE has independent CI, versioning, and
  Terraform. Teams own their repos end-to-end.
- **No shared build.** There is no monorepo build graph; coupling is explicit and runtime-only,
  via Feign clients (synchronous) and Kafka topics (asynchronous).
- **Contract-first.** `PLATFORM_SPEC.md` is the shared contract; each service commits its
  `api/openapi.yaml`. Cross-service calls only ever hit a target's `/internal/**` endpoints.
- **Reusable infra modules.** Repos consume `terraform-modules` (locally via a relative path; in a
  real polyrepo via a pinned `git::` source) and read `platform-infra` remote-state outputs.
- This directory checks all repos out side-by-side so `docker-compose` and the relative Terraform
  module sources resolve; in production each repo lives on its own.

---

## Prerequisites

- **Java 21** (Temurin) + **Maven** — backend services.
- **Node 20** — micro-frontends (Vite 5 / React 18).
- **Docker** + Docker Compose — local stack.
- **Terraform >= 1.6** + AWS credentials — infrastructure.

---

## Quickstart

### Local (Docker Compose)

```bash
make up        # postgres + kafka + all 15 services + 7 MFEs
make logs      # tail everything
make down      # stop and remove
```

The gateway is then at `http://localhost:8080`; the shell MFE at `http://localhost:3000`.
Postgres comes up with all 14 `*_db` databases pre-created (see `docker-compose.yml` /
`db-init`). Kafka is reachable at `localhost:9092` (host) / `kafka:9092` (compose network).

### Per-repo build

```bash
# a single Java service
cd order-service && mvn verify

# a single MFE
cd discovery-mfe && npm ci && npm run build
```

`make build-services` and `make build-mfes` build everything from the root.

### Terraform apply order

Infrastructure must be applied bottom-up so remote-state outputs and module sources resolve:

```
1. terraform-modules   # reusable modules (no state of its own; source-only)
2. platform-infra      # VPC, ECS cluster, ALB, ECR, RDS, MSK — exports remote state
3. services            # each java-service repo (reads platform-infra outputs)
4. mfes                # each react-mfe repo (S3 + CloudFront)
```

See `make tf-apply` for the helper target.

---

## Further reading

- [`PLATFORM_SPEC.md`](./PLATFORM_SPEC.md) — the canonical contract (authoritative).
- [`ARCHITECTURE.md`](./ARCHITECTURE.md) — narrative architecture, eventing, security, deployment.
- [`docs/dependency-graph.md`](./docs/dependency-graph.md) — Mermaid dependency + event-flow diagrams.
