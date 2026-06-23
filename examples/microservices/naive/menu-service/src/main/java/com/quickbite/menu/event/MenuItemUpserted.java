package com.quickbite.menu.event;

import com.quickbite.menu.domain.MenuItem;

import java.util.UUID;

/**
 * Payload of the {@code MenuItemUpserted} event emitted on create/update.
 * Consumed by search-service to build its denormalized index.
 */
public record MenuItemUpserted(
        UUID id,
        UUID restaurantId,
        String name,
        String description,
        long priceCents,
        String currency,
        String category,
        boolean available
) {
    public static MenuItemUpserted from(MenuItem item) {
        return new MenuItemUpserted(
                item.getId(),
                item.getRestaurantId(),
                item.getName(),
                item.getDescription(),
                item.getPriceCents(),
                item.getCurrency(),
                item.getCategory(),
                item.isAvailable());
    }
}
