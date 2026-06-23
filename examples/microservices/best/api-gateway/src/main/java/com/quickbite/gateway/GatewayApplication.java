package com.quickbite.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * QuickBite API Gateway — the single public edge for the platform.
 *
 * <p>Reactive Spring Cloud Gateway application. It routes {@code /api/**} traffic to the backend
 * services (see PLATFORM_SPEC §4), validates JWTs for protected routes and forwards
 * {@code X-User-Id} / {@code X-User-Role} headers downstream. It is the only WebFlux app in the
 * platform and is fully stateless (no JPA, no Postgres).
 */
@SpringBootApplication
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }
}
