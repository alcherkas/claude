package com.quickbite.menu.dto;

import com.quickbite.menu.domain.MenuItem;

import java.util.UUID;

/**
 * Slim service-to-service projection served at GET /internal/menu-items/{id}.
 * Consumed by cart-service, pricing-service and order-service.
 */
public record InternalMenuItemResponse(
        UUID id,
        UUID restaurantId,
        String name,
        long priceCents,
        String currency,
        boolean available
) {
    public static InternalMenuItemResponse from(MenuItem item) {
        return new InternalMenuItemResponse(
                item.getId(),
                item.getRestaurantId(),
                item.getName(),
                item.getPriceCents(),
                item.getCurrency(),
                item.isAvailable());
    }
}
