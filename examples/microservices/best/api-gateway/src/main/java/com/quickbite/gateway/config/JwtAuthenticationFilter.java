package com.quickbite.gateway.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Global edge authentication filter.
 *
 * <p>Rules (PLATFORM_SPEC §4):
 * <ul>
 *   <li>Anonymous: {@code /api/auth/**}, {@code /api/search/**} and GET on
 *       {@code /api/restaurants/**} + {@code /api/menu/**}.</li>
 *   <li>Everything else under {@code /api/**} requires a valid bearer JWT (shared HS256 secret).</li>
 *   <li>On success, {@code X-User-Id} and {@code X-User-Role} are injected downstream (and any
 *       client-supplied copies of those headers are stripped first, so they cannot be spoofed).</li>
 *   <li>{@code /internal/**} is never routable through the gateway — it is rejected with 404.</li>
 * </ul>
 */
@Slf4j
@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private static final AntPathMatcher MATCHER = new AntPathMatcher();
    private static final String USER_ID_HEADER = "X-User-Id";
    private static final String USER_ROLE_HEADER = "X-User-Role";

    /** Paths that bypass JWT validation entirely (any method). */
    private static final List<String> PUBLIC_PATHS = List.of(
            "/api/auth/**",
            "/api/search/**");

    /** Paths that bypass JWT validation only for safe (GET) reads. */
    private static final List<String> PUBLIC_GET_PATHS = List.of(
            "/api/restaurants/**",
            "/api/menu/**",
            "/api/gateway/**");

    private final JwtService jwtService;
    private final ObjectMapper objectMapper;

    public JwtAuthenticationFilter(JwtService jwtService, ObjectMapper objectMapper) {
        this.jwtService = jwtService;
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // /internal/** must never be reachable from the public edge.
        if (MATCHER.match("/internal/**", path)) {
            return deny(exchange, HttpStatus.NOT_FOUND, "Not found");
        }

        if (isAnonymous(request)) {
            // Still strip any inbound identity headers so anonymous traffic can't impersonate a user.
            ServerHttpRequest sanitized = request.mutate()
                    .headers(h -> {
                        h.remove(USER_ID_HEADER);
                        h.remove(USER_ROLE_HEADER);
                    })
                    .build();
            return chain.filter(exchange.mutate().request(sanitized).build());
        }

        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return deny(exchange, HttpStatus.UNAUTHORIZED, "Missing or malformed Authorization header");
        }

        String token = authHeader.substring(7).trim();
        try {
            Claims claims = jwtService.parse(token);
            String userId = claims.getSubject();
            Object role = claims.get("role");
            ServerHttpRequest mutated = request.mutate()
                    .headers(h -> {
                        h.remove(USER_ID_HEADER);
                        h.remove(USER_ROLE_HEADER);
                        h.set(USER_ID_HEADER, userId);
                        if (role != null) {
                            h.set(USER_ROLE_HEADER, role.toString());
                        }
                    })
                    .build();
            return chain.filter(exchange.mutate().request(mutated).build());
        } catch (JwtException | IllegalArgumentException ex) {
            log.debug("rejecting request to {} — invalid token: {}", path, ex.getMessage());
            return deny(exchange, HttpStatus.UNAUTHORIZED, "Invalid or expired token");
        }
    }

    private boolean isAnonymous(ServerHttpRequest request) {
        String path = request.getURI().getPath();
        if (PUBLIC_PATHS.stream().anyMatch(p -> MATCHER.match(p, path))) {
            return true;
        }
        boolean isGet = HttpMethod.GET.equals(request.getMethod());
        return isGet && PUBLIC_GET_PATHS.stream().anyMatch(p -> MATCHER.match(p, path));
    }

    private Mono<Void> deny(ServerWebExchange exchange, HttpStatus status, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", OffsetDateTime.now().toString());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        body.put("path", exchange.getRequest().getURI().getPath());

        byte[] bytes;
        try {
            bytes = objectMapper.writeValueAsBytes(body);
        } catch (Exception e) {
            bytes = ("{\"status\":" + status.value() + ",\"message\":\"" + message + "\"}")
                    .getBytes(StandardCharsets.UTF_8);
        }
        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        // Run before the routing filter so identity headers are set on the proxied request.
        return -100;
    }
}
