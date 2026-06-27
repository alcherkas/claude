package com.quickbite.order.dto;

import com.quickbite.order.domain.OrderStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Full public order representation. */
public record OrderResponse(
        UUID id,
        UUID userId,
        UUID restaurantId,
        OrderStatus status,
        long subtotalCents,
        long deliveryFeeCents,
        long serviceFeeCents,
        long taxCents,
        long discountCents,
        long tipCents,
        long totalCents,
        String currency,
        Instant createdAt,
        List<OrderItemResponse> items
) {
}
