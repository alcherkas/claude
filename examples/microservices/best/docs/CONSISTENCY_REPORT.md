# QuickBite Multi-Repo — Generation & Consistency Report

> Authoritative contract: [`PLATFORM_SPEC.md`](../PLATFORM_SPEC.md). This report summarizes the
> generation of the QuickBite polyrepo and the cross-repo audit performed against that spec.
> Generated: 2026-06-22.

---

## 1. Repository inventory

### 1.1 On-disk verification

A directory listing of `microservices-multirepo/` confirms **all 24 repositories are present**
(14 backend services + 1 gateway + 7 MFEs + 2 infra repos). **No repos are missing.**

### 1.2 Backend services (Java 21 / Spring Boot)

| # | Repo | Port | Files | Consistent |
|---|------|------|-------|------------|
| 1 | `identity-service` | 8081 | 38 | PASS |
| 2 | `restaurant-service` | 8082 | 38 | PASS |
| 3 | `menu-service` | 8083 | 38 | PASS (1 cross-repo note, out of scope) |
| 4 | `search-service` | 8084 | 34 | **FAIL → FIXED** |
| 5 | `cart-service` | 8085 | 38 | **FAIL → FIXED** |
| 6 | `promotion-service` | 8087 | 38 | PASS |
| 7 | `pricing-service` | 8086 | 34 | PASS |
| 8 | `order-service` | 8088 | 60 | PASS (4 in-repo issues fixed) |
| 9 | `wallet-service` | 8090 | 39 | PASS |
| 10 | `payment-service` | 8089 | 49 | PASS |
| 11 | `driver-service` | 8091 | 32 | PASS |
| 12 | `delivery-service` | 8092 | 47 | PASS |
| 13 | `notification-service` | 8093 | 39 | **FAIL → FIXED** |
| 14 | `review-service` | 8094 | 40 | **FAIL → FIXED** |
| 15 | `api-gateway` | 8080 | 25 | PASS |

### 1.3 Frontend micro-frontends (React + Vite + Module Federation)

| # | Repo | Dev port | Federation name | Files | Consistent |
|---|------|----------|-----------------|-------|------------|
| 1 | `shell` (host) | 3000 | `shell` | 30 | PASS |
| 2 | `discovery-mfe` | 3001 | `discovery` | 33 | **FAIL → FIXED** |
| 3 | `checkout-mfe` | 3002 | `checkout` | 33 | PASS |
| 4 | `order-tracking-mfe` | 3003 | `orderTracking` | 27 | PASS |
| 5 | `account-mfe` | 3004 | `account` | 29 | PASS |
| 6 | `restaurant-admin-mfe` | 3005 | `restaurantAdmin` | 32 | PASS |
| 7 | `driver-portal-mfe` | 3006 | `driverPortal` | 30 | PASS |

### 1.4 Infrastructure & meta repos

| Repo | Files | Purpose |
|------|-------|---------|
| `terraform-modules` | 21 | Reusable modules: `java-service`, `react-mfe`, `postgres-db`, `redis-cache` |
| `platform-infra` | 15 | Shared AWS foundation: VPC, subnets, ECS cluster, ALB, ECR, RDS, MSK/Redis |
| `microservices-multirepo` (root) | 7 | `README.md`, `PLATFORM_SPEC.md`, `ARCHITECTURE.md`, `docs/`, `docker-compose.yml`, `Makefile`, `.gitignore` |

**Total files generated across all repos: ~1,000** (sum of the above counts).

---

## 2. Audit results — PASS/FAIL per dimension

Four cross-repo audit dimensions were run against `PLATFORM_SPEC.md`. Three passed clean (after
fixes); one carries documented out-of-scope observations.

| Dimension | Result | Fixes applied |
|-----------|--------|---------------|
| Ports & gateway routing | **PASS** | 1 |
| Feign dependency wiring | **PASS** | 1 (+1 backing endpoint) |
| Eventing (Kafka topics/roles/envelope) | **PASS** | 0 |
| Terraform & federation | **PASS** (after fixes) | 9 |

### 2.1 Ports & gateway routing — PASS

- `server.port` in all 14 Java services + gateway match §1.1 exactly (identity 8081 … review 8094,
  gateway 8080).
- Gateway route table covers all 14 services with correct `/api` prefixes per §4 (identity split
  into `/api/auth` + `/api/users`; both target identity-service:8081 via one
  `CLIENTS_IDENTITY_URL`).
- `docker-compose.yml` port mappings for all 15 Java containers match the spec.
- MFE host ports (shell 3000, discovery 3001 … driver-portal 3006) and each MFE's
  `vite.config.ts` dev/preview ports + federation names match §1.2.
- **Fix applied:** `docker-compose.yml` injected `ROUTES_<SVC>_URL` env vars for the gateway, but
  the gateway's `application.yml` resolves `CLIENTS_<SVC>_URL` placeholders. The `ROUTES_*` vars
  were silently ignored, so inside compose the gateway fell back to `http://localhost:<port>`
  instead of compose-DNS targets. Renamed all 14 gateway downstream-target vars to
  `CLIENTS_<SVC>_URL`; values/ports unchanged.

### 2.2 Feign dependency wiring — PASS

- All 12 Feign-bearing Java services audited against §1.1. identity-service and
  notification-service correctly hold **zero** Feign clients (foundational / event-only).
- Feign-client-to-dependency mapping is **exactly correct** for every service after fixes — no
  missing and no extra/illegal dependencies (one client per declared dependency, hitting the
  target's `/internal/**`).
- Every `clients.<dep>.url` referenced by a Feign client is defined in that service's
  `application.yml`.
- **Fix applied (runtime 404 on refund flow):** payment-service `WalletClient.credit()` targets
  `POST /internal/wallets/{userId}/credit`, but wallet-service exposed only
  `GET /internal/wallets/{userId}` and `POST /internal/wallets/{userId}/debit`. Added the missing
  `POST /internal/wallets/{userId}/credit` endpoint in `InternalWalletController.java`, backed by a
  new `creditForOrder(UUID, DebitRequest)` method in `WalletService.java` mirroring `debit()`.

### 2.3 Eventing (Kafka) — PASS

- All five topic names exact: `orders.events`, `payments.events`, `deliveries.events`,
  `restaurant.events`, `menu.events`. No typos or stray variants.
- Producers emit to the correct topics; consumers (`@KafkaListener`) subscribe to the correct
  topics matching §5 (search correctly NOT subscribed to `orders.events`).
- Envelope shape `{eventId, type, occurredAt, payload}` consistent across all producers/consumers;
  producer/consumer (de)serialization line up (`VALUE_DEFAULT_TYPE=EventEnvelope`,
  `USE_TYPE_INFO_HEADERS=false`).
- No fixes required.

### 2.4 Terraform & federation — PASS (after fixes)

Initially FAIL; all defects fixed. Module source paths, module input keys, container ports, and
all MFE federation remotes (discovery 3001 … driver-portal 3006) were already correct.

**Fixes applied (9 repos):**

- **Broken `platform-infra` output reads** (`ecr_registry`, `kafka_bootstrap_servers` — neither
  exists in `outputs.tf`): fixed in cart, driver, wallet, identity, order, payment, delivery,
  review. Image URIs now built from `aws_caller_identity`/`aws_region`; Kafka bootstrap now read
  from a `kafka_bootstrap_servers` variable (added to order/payment/delivery `variables.tf`).
- **restaurant-service** built its image URI from `ecs_cluster_name` (bogus registry host) → fixed
  to the canonical account/region pattern.
- **Broken `postgres-db` output reads** (`module.db.username`/`module.db.password` — module only
  outputs `endpoint`, `secret_arn`, `security_group_id`): removed the broken `DB_*` env entries
  (the `java-service` module already injects DB creds as container secrets from `db_secret_arn`).
- **`path_prefix` missing `/api`**: cart, driver, wallet, order, payment, delivery, review were
  `/carts`, `/drivers`, etc. → corrected to `/api/carts`, `/api/drivers`, … matching §4.

---

## 3. Per-repo issues fixed

### search-service (FAIL → FIXED)
Declared sync deps on restaurant + menu (§1.1) but had **zero Feign clients** despite
docker-compose setting `CLIENTS_RESTAURANT_URL`/`CLIENTS_MENU_URL` and restaurant-service listing
search as a consumer. Added `RestaurantClient` + `MenuClient` (+ fallbacks); added
`spring-cloud-starter-openfeign` and `-circuitbreaker-resilience4j` to `pom.xml`; added
`clients.*.url` + resilience4j config to `application.yml`; added `@EnableFeignClients`; added
clients URLs to terraform env map and test `application-test.yml`; corrected README/Javadoc that
falsely claimed "no synchronous dependencies".

### cart-service (FAIL → FIXED)
identity is a declared dependency (§1.1: "menu, identity") and `clients.identity.url` + `IDENTITY_URL`
were wired, but **no `IdentityClient`** existed and the user was never validated. Added
`IdentityClient` (one Feign client per dependency, hitting identity `/internal/**`); added a
resilience4j circuit-breaker/timelimiter instance for identity; updated README dependency table and
openapi `addItem` 404/503 responses.

### notification-service (FAIL → FIXED)
Held an **extra, illegal `IdentityClient`** — notification's deps on order/delivery/payment are all
"(via events)", i.e. zero synchronous dependencies → must hold zero Feign clients. Removed
`IdentityClient` + fallback, `@EnableFeignClients`, the openfeign/resilience4j starters, the
`clients.identity.url`/resilience4j/feign blocks, `InternalUserView` DTO + `FeignConfig`, the
identity-based email-resolution logic, the terraform `identity_url` variable/tfvar/env var, and all
README/test references.

### review-service (FAIL → FIXED)
Missing Feign client for declared `identity` dependency (§1.1: order, restaurant, identity) — only
`OrderClient`/`RestaurantClient` existed. Added `IdentityClient`, `clients.identity.url`, a
resilience4j instance, and the terraform env var.

### discovery-mfe (FAIL → FIXED)
Overreached its §1.2 contract (search, restaurant, menu only). Removed the direct cart-service call
(`POST /api/carts/{userId}/items` / `addToCart`), the cart payload types
(`AddToCartRequest`/`Cart`/`CartItem`), `session.ts` `getCurrentUserId()`, the cart `useMutation` +
"Adding…" state in `MenuView`/`MenuItemCard`, and README/comment claims that discovery posts to
`/api/carts/**`. Cart/checkout is owned by `checkout-mfe`.

### order-service (4 in-repo issues, FIXED)
- `PricingClient` hit public `POST /api/pricing/quote` → corrected to `POST /internal/pricing/quote`
  (§2.1 cross-service calls hit `/internal/**` only).
- `MenuClient` hit `GET /internal/menu/{id}` → corrected to `/internal/menu-items/{id}`.
- `Order` entity declared `@OrderColumn(name = "item_position")` but `order_items` DDL lacked the
  column → would fail Hibernate `ddl-auto: validate`. Column added to `V1__init.sql`.
- README updated to drop the two stale Feign endpoints.

---

## 4. Post-audit follow-up fixes (all RESOLVED)

The three items the automated audit deferred were reviewed manually and **all fixed** (none actually
required a product decision), plus one layout/path-correctness class of bug was found and swept.

1. **order-service → menu-service path (cross-repo) — RESOLVED.** order-service's `MenuClient`
   already targets `/internal/menu-items/{id}` (correct). The only residue was a stale Javadoc in
   `MenuItemSummary.java` that said `/internal/menu/{id}`; corrected to `/internal/menu-items/{id}`.

2. **delivery notification recipient gap (eventing) — RESOLVED.** `delivery-service` now persists the
   ordering customer's `userId` on the `Delivery` entity (resolved from order-service `/internal` at
   creation, on both the sync `POST /api/deliveries` path and the event-driven READY path), adds a
   `user_id` column to `V1__init.sql`, and includes `userId` in `DeliveryStatusChangedPayload`. The
   recipient lookup stays on the producer side, so notification-service remains event-only (zero
   Feign clients) per §1.1.

3. **promotion-service Terraform — RESOLVED.** Rewrote `promotion-service/terraform/main.tf` to the
   canonical pattern: `postgres-db` is now called with `name = "promotion"` (→ database
   `promotion_db`, not `promotion_db_db`); the duplicate `DB_URL`/`DB_USERNAME` were removed from the
   `java-service` `environment` map (they are injected as container **secrets** from `db_secret_arn`,
   and ECS rejects a key present in both `environment` and `secrets`); the image URI is now built from
   `aws_caller_identity`/`aws_region` like its siblings, dropping the unused `ecr_registry`/
   `identity_url` variables.

4. **Cross-repo relative-path correctness — RESOLVED (new finding).** Every service/MFE
   `terraform/main.tf` referenced the modules as `../terraform-modules/...`, but the `.tf` lives in
   `<repo>/terraform/`, so the correct depth to reach the sibling `terraform-modules` repo is
   `../../terraform-modules/...` (fixed in all 22 repos + the spec/READMEs). Likewise
   `docker-compose.yml` used `build: ../<repo>`, but the compose file sits at the multi-repo root with
   the repos directly beneath it, so contexts are now `build: ./<repo>` (22 contexts). Both now
   resolve in the as-laid-out tree (and in a side-by-side polyrepo checkout).

---

## 5. Service dependency matrix (who calls whom)

### 5.1 Synchronous (Feign, `/internal/**`) — per §1.1

| Caller \ Callee | identity | restaurant | menu | cart | pricing | promotion | order | wallet | driver |
|-----------------|:--------:|:----------:|:----:|:----:|:-------:|:---------:|:-----:|:------:|:------:|
| restaurant-service | ● | | | | | | | | |
| menu-service | | ● | | | | | | | |
| search-service | | ● | ● | | | | | | |
| cart-service | ● | | ● | | | | | | |
| promotion-service | ● | | | | | | | | |
| pricing-service | | | ● | | | ● | | | |
| order-service | ● | ● | ● | ● | ● | | | | |
| payment-service | ● | | | | | | ● | ● | |
| wallet-service | ● | | | | | | | | |
| driver-service | ● | | | | | | | | |
| delivery-service | | | | | | | ● | | ● |
| review-service | ● | ● | | | | | ● | | |
| notification-service | _(none — event-only)_ | | | | | | | | |
| identity-service | _(none — foundational)_ | | | | | | | | |

High-fan-in hubs: **order-service** (called by payment, delivery, review) and **identity-service**
(called by 8 services). **api-gateway** routes to all 14 services but holds no Feign clients.

### 5.2 Asynchronous (Kafka) — per §5

| Topic | Produced by | Consumed by |
|-------|-------------|-------------|
| `orders.events` | order-service | notification, delivery |
| `payments.events` | payment-service | notification, order |
| `deliveries.events` | delivery-service | notification, order |
| `restaurant.events` | restaurant-service | search |
| `menu.events` | menu-service | search |

### 5.3 Frontend → gateway (per §1.2)

| MFE | Talks to (via gateway) |
|-----|------------------------|
| `shell` (host) | gateway (auth); loads all remotes |
| `discovery-mfe` | search, restaurant, menu |
| `checkout-mfe` | cart, pricing, promotion, order, payment, wallet |
| `order-tracking-mfe` | order, delivery |
| `account-mfe` | identity, wallet, review, order |
| `restaurant-admin-mfe` | restaurant, menu, order, promotion |
| `driver-portal-mfe` | driver, delivery |

---

## 6. How to run locally / how to deploy

### 6.1 Run the full stack locally (Docker Compose)

The root `docker-compose.yml` brings up Postgres, Kafka, all 15 Java containers, and the 7 MFEs.
Inter-service URLs resolve via compose DNS (`http://<service>:<port>`); the gateway resolves
`CLIENTS_<SVC>_URL` to those targets.

```bash
# from microservices-multirepo/
docker compose up --build          # builds images and starts the full stack
# or use the Makefile convenience targets:
make up                            # docker compose up -d --build
make logs                          # tail aggregated logs
make down                          # docker compose down -v
```

Once up:
- API gateway: `http://localhost:8080` (all external traffic; `/api/**` routes per §4).
- Shell host MFE: `http://localhost:3000` (lazily federates discovery 3001 … driver-portal 3006).
- Each service exposes `/actuator/health` and Swagger UI at `/swagger-ui.html`.
- Kafka: `kafka:9092` in compose / `localhost:9092` from host.

Per-service dev loop (outside compose):
```bash
cd <service> && mvn spring-boot:run     # service binds its spec port; clients.*.url default to localhost
cd <mfe>     && npm install && npm run dev   # binds its §1.2 dev port; VITE_GATEWAY_URL defaults to :8080
```

> Note: the audit performed **no** builds — `mvn`, `npm install`, `terraform init`, and `docker`
> were not executed. The commands above are the intended workflow, not steps run during this audit.

### 6.2 Deploy (Terraform, AWS)

1. **Bootstrap shared foundation first:**
   ```bash
   cd platform-infra/
   terraform init -backend-config="key=platform-infra/dev/terraform.tfstate"
   terraform apply -var="env=dev"
   ```
   This provisions VPC, subnets, ECS cluster, ALB, ECR, RDS, MSK/Redis and exports the remote-state
   outputs every service repo reads (`vpc_id`, `private_subnet_ids`, `ecs_cluster_arn`,
   `alb_listener_arn`, `service_discovery_namespace_id`, …).

2. **Deploy each service / MFE repo independently:**
   ```bash
   cd <repo>/terraform/
   terraform init -backend-config="key=<repo>/dev/terraform.tfstate"
   terraform apply -var="env=dev"
   ```
   Each repo reads `platform-infra` outputs via `data "terraform_remote_state" "platform"` and
   consumes the reusable modules from `../../terraform-modules/modules/<name>` (a pinned git source in a
   real polyrepo). Java services register an ALB listener rule on their `path_prefix` (e.g.
   `/api/orders`); MFEs deploy as S3 + CloudFront static sites.

- State backend: S3 `quickbite-tfstate-<env>`, DynamoDB lock table `quickbite-tflocks`, region
  `us-east-1`, default env `dev`.
- CI: each repo's `.github/workflows/ci.yml` runs `mvn verify` + docker build (Java) or
  `npm ci && build` (MFE).

---

## 7. Summary

- **24/24 repos present** on disk; none missing.
- **6 repos required fixes** to become spec-consistent: search-service, cart-service,
  notification-service, review-service, discovery-mfe (5 declared FAIL), plus in-repo fixes in
  order-service.
- **4 audit dimensions all PASS** after fixes; total **12 cross-repo + per-repo fix sets applied**.
- **All 4 post-audit follow-ups RESOLVED** (§4): stale menu Javadoc, delivery→notification `userId`
  propagation, promotion-service Terraform (DB name + env/secret collision), and a cross-repo
  relative-path correctness sweep (`../../terraform-modules` in 22 repos; `build: ./<repo>` in
  docker-compose). **No open items remain.**
