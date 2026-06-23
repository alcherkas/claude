package com.quickbite.gateway.dto;

/**
 * One configured gateway route: the public path prefix and the backend service URI it proxies to.
 * Surfaced on the {@code /internal/routes} endpoint for platform diagnostics.
 */
public record RouteSummary(
        String routeId,
        String pathPrefix,
        String targetUri) {
}
