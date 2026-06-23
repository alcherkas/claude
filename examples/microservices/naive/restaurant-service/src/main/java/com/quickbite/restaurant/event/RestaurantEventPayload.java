package com.quickbite.restaurant.event;

import com.quickbite.restaurant.domain.Restaurant;
import com.quickbite.restaurant.domain.RestaurantStatus;

import java.util.UUID;

/**
 * Denormalized payload carried by {@code RestaurantUpserted} and
 * {@code RestaurantStatusChanged} events on the {@code restaurant.events} topic.
 */
public record RestaurantEventPayload(
        UUID id,
        UUID ownerUserId,
        String name,
        String cuisine,
        String city,
        Double lat,
        Double lng,
        RestaurantStatus status
) {
    public static RestaurantEventPayload from(Restaurant r) {
        return new RestaurantEventPayload(
                r.getId(),
                r.getOwnerUserId(),
                r.getName(),
                r.getCuisine(),
                r.getCity(),
                r.getLat(),
                r.getLng(),
                r.getStatus()
        );
    }
}
