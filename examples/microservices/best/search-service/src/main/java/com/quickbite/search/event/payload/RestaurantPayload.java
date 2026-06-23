package com.quickbite.search.event.payload;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.UUID;

/**
 * Subset of the {@code restaurant.events} payload the search index cares about.
 * Emitted by restaurant-service for {@code RestaurantUpserted} / {@code RestaurantStatusChanged}.
 * Unknown fields are ignored so producer additions never break the consumer.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record RestaurantPayload(
        UUID id,
        String name,
        String cuisine,
        Double lat,
        Double lng,
        String status
) {
    /** A restaurant is searchable only while ACTIVE. */
    public boolean isAvailable() {
        return "ACTIVE".equalsIgnoreCase(status);
    }
}
