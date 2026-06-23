package com.quickbite.restaurant.web;

import com.quickbite.restaurant.dto.RestaurantSummary;
import com.quickbite.restaurant.service.RestaurantService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Service-to-service API. Never exposed through the gateway; consumed by
 * menu-service, search-service, order-service, review-service, etc.
 */
@RestController
@RequestMapping("/internal/restaurants")
public class InternalRestaurantController {

    private final RestaurantService service;

    public InternalRestaurantController(RestaurantService service) {
        this.service = service;
    }

    @GetMapping("/{id}")
    public RestaurantSummary getSummary(@PathVariable UUID id) {
        return service.getSummary(id);
    }
}
