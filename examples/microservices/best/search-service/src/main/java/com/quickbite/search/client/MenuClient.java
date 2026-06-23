package com.quickbite.search.client;

import com.quickbite.search.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

/**
 * Feign client to menu-service (PLATFORM_SPEC §1.1 dependency). Cross-service calls
 * only ever hit the dependency's {@code /internal/**} endpoints.
 */
@FeignClient(
        name = "menu-service",
        url = "${clients.menu.url}",
        configuration = FeignConfig.class,
        fallbackFactory = MenuClientFallbackFactory.class
)
public interface MenuClient {

    @GetMapping("/internal/menu-items/{id}")
    MenuItemView getMenuItem(@PathVariable("id") UUID id);
}
