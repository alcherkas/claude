package com.quickbite.cart.client;

import com.quickbite.cart.config.FeignConfig;
import com.quickbite.cart.dto.MenuItemView;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Feign client to menu-service. Cart-service re-prices items against the
 * authoritative menu via the dependency's /internal endpoints.
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
