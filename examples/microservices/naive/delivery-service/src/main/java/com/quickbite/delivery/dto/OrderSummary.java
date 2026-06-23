package com.quickbite.delivery.dto;

import java.util.UUID;

/**
 * Mirror of order-service's {@code GET /internal/orders/{id}} response
 * (id, userId, restaurantId, status, totalCents, currency).
 */
public record OrderSummary(
        UUID id,
        UUID userId,
        UUID restaurantId,
        String status,
        long totalCents,
        String currency
) {
}
