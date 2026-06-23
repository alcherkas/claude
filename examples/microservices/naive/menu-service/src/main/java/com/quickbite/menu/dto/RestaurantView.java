package com.quickbite.menu.dto;

import java.util.UUID;

/**
 * Projection of a restaurant as returned by restaurant-service
 * GET /internal/restaurants/{id}. Only the fields menu-service needs are mapped;
 * unknown JSON properties are ignored by Jackson.
 */
public record RestaurantView(
        UUID id,
        UUID ownerUserId,
        String name,
        String status
) {
    public boolean isActive() {
        return "ACTIVE".equalsIgnoreCase(status);
    }
}
