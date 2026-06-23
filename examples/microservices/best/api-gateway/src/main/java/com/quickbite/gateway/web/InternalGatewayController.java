package com.quickbite.gateway.web;

import com.quickbite.gateway.dto.RouteSummary;
import com.quickbite.gateway.service.GatewayInfoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * Internal-only diagnostics for the gateway, served at {@code /internal/**}.
 *
 * <p>Per PLATFORM_SPEC §4 the {@code /internal/**} space is never reachable through the public edge
 * — the {@link com.quickbite.gateway.config.JwtAuthenticationFilter} rejects any inbound
 * {@code /internal/**} request with 404. These endpoints are intended for platform tooling reaching
 * the gateway pod directly (e.g. service-mesh / ops scripts).
 */
@RestController
@RequestMapping("/internal")
public class InternalGatewayController {

    private final GatewayInfoService gatewayInfoService;

    public InternalGatewayController(GatewayInfoService gatewayInfoService) {
        this.gatewayInfoService = gatewayInfoService;
    }

    /** Full list of configured routes (id, public prefix, backend target URI). */
    @GetMapping("/routes")
    public Flux<RouteSummary> routes() {
        return gatewayInfoService.routes();
    }
}
