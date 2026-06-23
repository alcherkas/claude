package com.quickbite.gateway.dto;

import java.util.List;

/**
 * Public, anonymous-safe description of the gateway edge: which prefixes it exposes and which are
 * readable without authentication. Returned by {@code GET /api/gateway/info}.
 */
public record GatewayInfo(
        String name,
        String version,
        List<String> routes,
        List<String> publicPaths) {
}
