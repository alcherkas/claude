package com.quickbite.menu.client;

import com.quickbite.menu.config.FeignConfig;
import com.quickbite.menu.dto.RestaurantView;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

/**
 * Feign client to restaurant-service. Targets only its /internal API.
 * URL comes from {@code clients.restaurant.url}. Circuit-broken with a fallback.
 */
@FeignClient(
        name = "restaurant",
        url = "${clients.restaurant.url}",
        configuration = FeignConfig.class,
        fallbackFactory = RestaurantClientFallbackFactory.class
)
public interface RestaurantClient {

    @GetMapping("/internal/restaurants/{id}")
    RestaurantView getRestaurant(@PathVariable("id") UUID id);
}
