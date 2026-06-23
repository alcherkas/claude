package com.quickbite.menu.dto;

import com.quickbite.menu.domain.MenuItem;

import java.time.Instant;
import java.util.UUID;

/**
 * Full public representation of a menu item (GET /api/menu, POST /api/menu, etc.).
 */
public record MenuItemResponse(
        UUID id,
        UUID restaurantId,
        String name,
        String description,
        long priceCents,
        String currency,
        String category,
        boolean available,
        Instant createdAt
) {
    public static MenuItemResponse from(MenuItem item) {
        return new MenuItemResponse(
                item.getId(),
                item.getRestaurantId(),
                item.getName(),
                item.getDescription(),
                item.getPriceCents(),
                item.getCurrency(),
                item.getCategory(),
                item.isAvailable(),
                item.getCreatedAt());
    }
}
