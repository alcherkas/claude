package com.quickbite.payment.dto;

import java.util.UUID;

/**
 * Mirror of order-service's GET /internal/orders/{id} response. Provides the
 * authoritative amount this payment must charge.
 */
public record OrderSummary(
        UUID id,
        UUID userId,
        UUID restaurantId,
        String status,
        long tipCents,
        long totalCents,
        String currency
) {
}
