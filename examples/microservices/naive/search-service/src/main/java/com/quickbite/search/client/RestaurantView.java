package com.quickbite.search.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.UUID;

/**
 * Projection returned by restaurant-service {@code GET /internal/restaurants/{id}}
 * (mirrors its {@code RestaurantSummary}). Used to backfill geo/cuisine into the
 * search index when a menu-item event arrives before its parent restaurant event.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record RestaurantView(
        UUID id,
        String name,
        String status,
        Double lat,
        Double lng,
        String cuisine
) {
    /** A restaurant is searchable only while ACTIVE. */
    public boolean isAvailable() {
        return "ACTIVE".equalsIgnoreCase(status);
    }
}
