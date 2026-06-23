package com.quickbite.search.client;

import com.quickbite.search.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

/**
 * Feign client to restaurant-service (PLATFORM_SPEC §1.1 dependency). Cross-service calls
 * only ever hit the dependency's {@code /internal/**} endpoints.
 */
@FeignClient(
        name = "restaurant-service",
        url = "${clients.restaurant.url}",
        configuration = FeignConfig.class,
        fallbackFactory = RestaurantClientFallbackFactory.class
)
public interface RestaurantClient {

    @GetMapping("/internal/restaurants/{id}")
    RestaurantView getRestaurant(@PathVariable("id") UUID id);
}
