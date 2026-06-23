package com.quickbite.review.dto;

import java.time.Instant;
import java.util.UUID;

/** Public response shape for a review. */
public record ReviewResponse(
        UUID id,
        UUID orderId,
        UUID userId,
        UUID restaurantId,
        UUID driverId,
        int rating,
        String comment,
        Instant createdAt
) {
}
