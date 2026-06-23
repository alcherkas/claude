package com.quickbite.restaurant.dto;

import com.quickbite.restaurant.domain.Restaurant;
import com.quickbite.restaurant.domain.RestaurantStatus;

import java.util.UUID;

/**
 * Compact projection returned by {@code GET /internal/restaurants/{id}} for
 * consumption by menu-service, search-service, order-service, etc.
 */
public record RestaurantSummary(
        UUID id,
        String name,
        RestaurantStatus status,
        Double lat,
        Double lng,
        String cuisine
) {
    public static RestaurantSummary from(Restaurant r) {
        return new RestaurantSummary(
                r.getId(),
                r.getName(),
                r.getStatus(),
                r.getLat(),
                r.getLng(),
                r.getCuisine()
        );
    }
}
