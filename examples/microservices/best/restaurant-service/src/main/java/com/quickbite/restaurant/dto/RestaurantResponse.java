package com.quickbite.restaurant.dto;

import com.quickbite.restaurant.domain.Restaurant;
import com.quickbite.restaurant.domain.RestaurantStatus;

import java.time.Instant;
import java.util.UUID;

public record RestaurantResponse(
        UUID id,
        UUID ownerUserId,
        String name,
        String cuisine,
        String addressLine,
        String city,
        Double lat,
        Double lng,
        RestaurantStatus status,
        String openingHours,
        Instant createdAt
) {
    public static RestaurantResponse from(Restaurant r) {
        return new RestaurantResponse(
                r.getId(),
                r.getOwnerUserId(),
                r.getName(),
                r.getCuisine(),
                r.getAddressLine(),
                r.getCity(),
                r.getLat(),
                r.getLng(),
                r.getStatus(),
                r.getOpeningHours(),
                r.getCreatedAt()
        );
    }
}
