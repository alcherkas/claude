package com.quickbite.order.dto;

import java.util.List;
import java.util.UUID;

/** Shape returned by cart-service GET /internal/carts/{userId}/snapshot. */
public record CartSnapshot(
        UUID userId,
        UUID restaurantId,
        List<CartSnapshotItem> items
) {
    public record CartSnapshotItem(
            UUID menuItemId,
            String name,
            int qty,
            long unitPriceCents
    ) {
    }
}
