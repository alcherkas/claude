package com.quickbite.gateway;

import static org.assertj.core.api.Assertions.assertThat;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * Context-load test plus a WebTestClient slice exercising the public {@code /api/gateway/info}
 * endpoint and the edge auth filter. The downstream services are not running, so we only assert on
 * behaviour the gateway owns locally (info endpoint, anonymous access, and 401 on a bad token).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@TestPropertySource(properties = {
        "JWT_SECRET=test-secret-test-secret-test-secret-test-secret-32b",
        "JWT_ISSUER=quickbite-identity"
})
class GatewayApplicationTests {

    private static final String SECRET = "test-secret-test-secret-test-secret-test-secret-32b";

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private RouteDefinitionLocator routeDefinitionLocator;

    @Test
    void contextLoads() {
        assertThat(routeDefinitionLocator).isNotNull();
    }

    @Test
    void allFourteenBackendRoutesAreConfigured() {
        long routeCount = routeDefinitionLocator.getRouteDefinitions().count().block();
        // 14 services, with identity split into auth + users routes => 15 route definitions.
        assertThat(routeCount).isEqualTo(15);
    }

    @Test
    void gatewayInfoIsPubliclyReadable() {
        webTestClient.get().uri("/api/gateway/info")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.name").isEqualTo("api-gateway")
                .jsonPath("$.publicPaths").exists();
    }

    @Test
    void protectedRouteWithoutTokenIsRejected() {
        webTestClient.get().uri("/api/orders/1")
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody()
                .jsonPath("$.status").isEqualTo(401)
                .jsonPath("$.path").isEqualTo("/api/orders/1");
    }

    @Test
    void internalPathsAreNeverRoutable() {
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        String token = Jwts.builder()
                .issuer("quickbite-identity")
                .subject("42")
                .claim("role", "CUSTOMER")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(key)
                .compact();

        // /internal/routes exists as a local controller, but the edge filter blocks /internal/** with 404.
        webTestClient.get().uri("/internal/routes")
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isNotFound();
    }
}
