# identity-service

Foundational authentication and user-account service for the **QuickBite** food-delivery
platform. It owns user records and is the platform's **JWT issuer** (HS256). The api-gateway and
every downstream service trust tokens signed with the shared `JWT_SECRET`, so identity is never on
the hot path for request authorization — it only mints and (internally) validates accounts.

- **Port:** `8081`
- **Database:** `identity_db` (Postgres, Flyway-managed)
- **Base package:** `com.quickbite.identity`
- **Event role:** none
- **Sync dependencies:** none (foundational)
- **Gateway routes:** `/api/auth/**`, `/api/users/**`

## What it does

- Registers accounts and hashes passwords with **BCrypt**.
- Authenticates credentials and issues an HS256 JWT carrying `sub` (user id), `email`, `role`, `name`.
- Resolves the current user from a bearer token.
- Exposes service-to-service lookup and validation under `/internal/**` (never gateway-exposed).

## Domain

`User { id, email (unique), passwordHash, fullName, role, active, createdAt }`
where `role ∈ { CUSTOMER, RESTAURANT_OWNER, COURIER, ADMIN }`.

## Dependencies (Maven)

Spring Boot 3.3.2 / Spring Cloud 2023.0.3 (BOM) / Java 21:
`spring-boot-starter-web`, `-validation`, `-actuator`, `-security`, `-data-jpa`;
`postgresql` + `flyway-core` (+ `flyway-database-postgresql`);
`io.jsonwebtoken:jjwt-api/-impl/-jackson` 0.12.6;
`springdoc-openapi-starter-webmvc-ui` 2.6.0; Lombok. Tests: `spring-boot-starter-test`,
`spring-security-test`, H2.

## Endpoints

### Public (via gateway under `/api`)

| Method | Path                 | Description                                  | Auth   |
|--------|----------------------|----------------------------------------------|--------|
| POST   | `/api/auth/register` | Create an account → `UserResponse` (201)     | none   |
| POST   | `/api/auth/login`    | Authenticate → `{ token, expiresAt, user }`  | none   |
| GET    | `/api/users/me`      | Current user from bearer token               | bearer |
| GET    | `/api/users/{id}`    | Fetch a user by id                           | bearer |

### Internal (service-to-service only, never via gateway)

| Method | Path                                      | Description                                                 |
|--------|-------------------------------------------|-------------------------------------------------------------|
| GET    | `/internal/users/{id}`                    | `{ id, email, fullName, role, active }`                     |
| GET    | `/internal/users/{id}/validate?role=ROLE` | `{ valid }` — exists, active, and (optionally) role matches |

### Ops

- Health: `GET /actuator/health`
- OpenAPI JSON: `GET /v3/api-docs` · Swagger UI: `GET /swagger-ui.html`
- Committed contract: [`api/openapi.yaml`](api/openapi.yaml)

## Configuration

All values are overridable via environment variables (defaults in `application.yml`):

| Env var           | Default                                   | Notes                                |
|-------------------|-------------------------------------------|--------------------------------------|
| `SERVER_PORT`     | `8081`                                     |                                      |
| `DB_URL`          | `jdbc:postgresql://localhost:5432/identity_db` |                                 |
| `DB_USERNAME`     | `identity`                                 |                                      |
| `DB_PASSWORD`     | `identity`                                 |                                      |
| `JWT_SECRET`      | dev placeholder                            | **shared** with gateway + services   |
| `JWT_ISSUER`      | `quickbite-identity`                       |                                      |
| `JWT_TTL_SECONDS` | `86400`                                    | token lifetime                       |

Security permits `/api/auth/**`, `/internal/**`, `/actuator/**`, `/v3/api-docs/**`,
`/swagger-ui/**`; all other routes require a valid bearer token.

## Run locally

```bash
# Start Postgres (e.g. docker run -e POSTGRES_DB=identity_db -e POSTGRES_USER=identity \
#   -e POSTGRES_PASSWORD=identity -p 5432:5432 postgres:16)
mvn spring-boot:run
# → http://localhost:8081/swagger-ui.html
```

Example:

```bash
curl -X POST localhost:8081/api/auth/register -H 'Content-Type: application/json' \
  -d '{"email":"ada@quickbite.dev","password":"password1","fullName":"Ada Lovelace","role":"CUSTOMER"}'

curl -X POST localhost:8081/api/auth/login -H 'Content-Type: application/json' \
  -d '{"email":"ada@quickbite.dev","password":"password1"}'
```

## Build & test

```bash
mvn -B verify          # compile + run tests (context load + auth slice)
mvn -B clean package   # build the jar
```

## Docker

```bash
docker build -t quickbite/identity-service:local .
docker run -p 8081:8081 \
  -e DB_URL=jdbc:postgresql://host.docker.internal:5432/identity_db \
  -e JWT_SECRET=your-shared-secret \
  quickbite/identity-service:local
```

## Deploy (Terraform)

The `terraform/` dir reads `platform-infra` remote state and consumes the shared
`java-service` and `postgres-db` modules from `terraform-modules`.

```bash
cd terraform
terraform init \
  -backend-config="bucket=quickbite-tfstate-dev" \
  -backend-config="key=identity-service/dev/terraform.tfstate" \
  -backend-config="region=us-east-1" \
  -backend-config="dynamodb_table=quickbite-tflocks"
terraform apply -var="image_tag=$(git rev-parse --short HEAD)" -var="jwt_secret=…"
```

The `java-service` module wires an ALB listener rule on `path_prefix=/api/auth` and runs the
container on port `8081` with health checks at `/actuator/health`.
