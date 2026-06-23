package com.quickbite.search.event.payload;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.UUID;

/**
 * Subset of the {@code menu.events} payload the search index cares about.
 * Emitted by menu-service for {@code MenuItemUpserted}.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record MenuItemPayload(
        UUID id,
        UUID restaurantId,
        String name,
        String category,
        Long priceCents,
        boolean available
) {
}
