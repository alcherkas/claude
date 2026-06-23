package com.quickbite.order.client;

import com.quickbite.order.dto.RestaurantSummary;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "restaurantClient", url = "${clients.restaurant.url}", fallback = RestaurantClientFallback.class)
public interface RestaurantClient {

    @GetMapping("/internal/restaurants/{id}")
    RestaurantSummary getRestaurant(@PathVariable("id") UUID id);
}
