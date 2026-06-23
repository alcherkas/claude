package com.quickbite.search.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.UUID;

/**
 * Projection returned by menu-service {@code GET /internal/menu-items/{id}}
 * (mirrors its {@code InternalMenuItemResponse}). Used to backfill a menu-item
 * document if its {@code MenuItemUpserted} event was missed.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record MenuItemView(
        UUID id,
        UUID restaurantId,
        String name,
        long priceCents,
        String currency,
        boolean available
) {
}
