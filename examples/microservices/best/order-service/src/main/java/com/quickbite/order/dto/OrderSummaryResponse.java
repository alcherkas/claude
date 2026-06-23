package com.quickbite.order.dto;

import com.quickbite.order.domain.OrderStatus;

import java.util.UUID;

/** Internal summary returned by GET /internal/orders/{id}. */
public record OrderSummaryResponse(
        UUID id,
        UUID userId,
        UUID restaurantId,
        OrderStatus status,
        long totalCents,
        String currency
) {
}
