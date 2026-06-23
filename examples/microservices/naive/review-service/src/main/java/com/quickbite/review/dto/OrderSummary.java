package com.quickbite.review.dto;

import java.util.UUID;

/**
 * Shape returned by order-service GET /internal/orders/{id}.
 * Only the fields review-service needs to verify ownership and delivery state.
 */
public record OrderSummary(
        UUID id,
        UUID userId,
        UUID restaurantId,
        String status,
        long totalCents,
        String currency
) {
    public boolean isDelivered() {
        return "DELIVERED".equalsIgnoreCase(status);
    }
}
