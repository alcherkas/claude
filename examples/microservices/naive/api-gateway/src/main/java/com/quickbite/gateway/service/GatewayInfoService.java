package com.quickbite.gateway.service;

import com.quickbite.gateway.dto.GatewayInfo;
import com.quickbite.gateway.dto.RouteSummary;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Reactive introspection over the gateway's configured routes. Reads the
 * {@link RouteDefinitionLocator} (populated from {@code application.yml}) and exposes a thin,
 * read-only summary used by the public info endpoint and the internal diagnostics endpoint.
 */
@Service
public class GatewayInfoService {

    private static final List<String> PUBLIC_PATHS = List.of(
            "/api/auth/**",
            "/api/search/**",
            "GET /api/restaurants/**",
            "GET /api/menu/**");

    private final RouteDefinitionLocator routeDefinitionLocator;
    private final String name;
    private final String version;

    public GatewayInfoService(RouteDefinitionLocator routeDefinitionLocator,
                              @Value("${spring.application.name:api-gateway}") String name) {
        this.routeDefinitionLocator = routeDefinitionLocator;
        this.name = name;
        this.version = "1.0.0";
    }

    /** All configured routes, flattened into id / path-prefix / target-uri summaries. */
    public Flux<RouteSummary> routes() {
        return routeDefinitionLocator.getRouteDefinitions()
                .map(this::toSummary);
    }

    /** Public, anonymous-safe edge description. */
    public Mono<GatewayInfo> info() {
        return routes()
                .map(RouteSummary::pathPrefix)
                .collectList()
                .map(prefixes -> new GatewayInfo(name, version, prefixes, PUBLIC_PATHS));
    }

    private RouteSummary toSummary(RouteDefinition definition) {
        String pathPrefix = definition.getPredicates().stream()
                .filter(p -> "Path".equalsIgnoreCase(p.getName()))
                .map(p -> p.getArgs().values().stream().findFirst().orElse(""))
                .findFirst()
                .orElse("");
        String target = definition.getUri() == null ? "" : definition.getUri().toString();
        return new RouteSummary(definition.getId(), pathPrefix, target);
    }
}
