package com.quickbite.gateway.web;

import com.quickbite.gateway.dto.GatewayInfo;
import com.quickbite.gateway.service.GatewayInfoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * Public gateway endpoint, reachable through the edge itself at {@code /api/gateway/**}.
 * Anonymous-safe: only exposes non-sensitive routing metadata, never any downstream secret.
 */
@RestController
@RequestMapping("/api/gateway")
public class GatewayController {

    private final GatewayInfoService gatewayInfoService;

    public GatewayController(GatewayInfoService gatewayInfoService) {
        this.gatewayInfoService = gatewayInfoService;
    }

    /** Describes the edge: configured route prefixes and which paths are publicly readable. */
    @GetMapping("/info")
    public Mono<GatewayInfo> info() {
        return gatewayInfoService.info();
    }
}
