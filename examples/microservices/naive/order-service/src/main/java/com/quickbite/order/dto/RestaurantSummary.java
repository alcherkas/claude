package com.quickbite.order.dto;

import java.util.UUID;

/** Shape returned by restaurant-service GET /internal/restaurants/{id}. */
public record RestaurantSummary(
        UUID id,
        String name,
        String status
) {
    public boolean isActive() {
        return "ACTIVE".equalsIgnoreCase(status);
    }
}
