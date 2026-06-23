package com.quickbite.pricing.client;

import com.quickbite.pricing.config.FeignConfig;
import com.quickbite.pricing.dto.MenuItemView;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Feign client to menu-service (port 8083). Fetches authoritative prices via the
 * target's internal endpoint. URL comes from {@code clients.menu.url}.
 */
@FeignClient(
        name = "menu-service",
        url = "${clients.menu.url}",
        configuration = FeignConfig.class,
        fallbackFactory = MenuClientFallbackFactory.class
)
public interface MenuClient {

    @GetMapping("/internal/menu-items/{id}")
    MenuItemView getMenuItem(@PathVariable("id") Long id);
}
