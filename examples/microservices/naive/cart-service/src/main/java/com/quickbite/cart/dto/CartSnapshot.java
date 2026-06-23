package com.quickbite.cart.dto;

import java.util.List;

/**
 * Immutable snapshot of a cart produced at checkout and consumed by order-service
 * via the /internal endpoint. Carries the authoritative line items and subtotal.
 */
public record CartSnapshot(
        Long userId,
        Long restaurantId,
        List<SnapshotItem> items,
        long subtotalCents
) {
    public record SnapshotItem(
            Long menuItemId,
            String name,
            int qty,
            long unitPriceCents
    ) {
    }
}
