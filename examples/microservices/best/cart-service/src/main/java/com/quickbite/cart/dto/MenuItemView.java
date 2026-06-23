package com.quickbite.cart.dto;

/**
 * Projection of a menu item as returned by menu-service
 * GET /internal/menu-items/{id}. Used to re-price cart items.
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
