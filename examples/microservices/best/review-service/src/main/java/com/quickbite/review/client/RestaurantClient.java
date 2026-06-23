package com.quickbite.review.client;

import com.quickbite.review.dto.RestaurantSummary;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

/** Optional dependency: used to confirm the restaurant exists when enrichment is desired. */
@FeignClient(name = "restaurantClient", url = "${clients.restaurant.url}", fallback = RestaurantClientFallback.class)
public interface RestaurantClient {

    @GetMapping("/internal/restaurants/{id}")
    RestaurantSummary getRestaurant(@PathVariable("id") UUID id);
}
