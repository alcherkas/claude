package com.quickbite.search.dto;

import com.quickbite.search.domain.SearchDoc;

import java.time.Instant;
import java.util.UUID;

/** A single hit returned from a search query. */
public record SearchResultDto(
        UUID id,
        String type,
        UUID refId,
        UUID restaurantId,
        String name,
        String cuisine,
        Long priceCents,
        Double lat,
        Double lng,
        boolean available,
        Double distanceKm,
        Instant updatedAt
) {
    public static SearchResultDto from(SearchDoc d, Double distanceKm) {
        return new SearchResultDto(
                d.getId(),
                d.getType().name(),
                d.getRefId(),
                d.getRestaurantId(),
                d.getName(),
                d.getCuisine(),
                d.getPriceCents(),
                d.getLat(),
                d.getLng(),
                d.isAvailable(),
                distanceKm,
                d.getUpdatedAt()
        );
    }
}
