package com.quickbite.pricing.dto;

/**
 * Projection of a menu item as returned by menu-service
 * {@code GET /internal/menu-items/{id}}.
 */
public record MenuItemView(
        Long id,
        Long restaurantId,
        String name,
        long priceCents,
        String currency,
        boolean available
) {
}
