# CLAUDE.md

QuickBite — a food-delivery platform. This folder has a bunch of backend services and
some React frontends.

## Stack
- Backend services are Java / Spring Boot (Maven).
- Frontends are React + Vite.
- Local stack runs with Docker Compose.

## Running it
```bash
make up      # start everything
make logs    # tail logs
make down    # stop
```

## Notes
- Each service has its own folder, its own README, and its own Dockerfile.
- See README.md and ARCHITECTURE.md for more.
- Services talk to each other over HTTP and there's some Kafka involved.
