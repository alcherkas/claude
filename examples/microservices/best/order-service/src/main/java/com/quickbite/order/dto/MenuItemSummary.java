package com.quickbite.order.dto;

import java.util.UUID;

/** Shape returned by menu-service GET /internal/menu-items/{id}. */
public record MenuItemSummary(
        UUID id,
        UUID restaurantId,
        String name,
        long priceCents,
        String currency,
        boolean available
) {
}
