package com.quickbite.order.dto;

import java.util.UUID;

public record OrderItemResponse(
        UUID id,
        UUID menuItemId,
        String name,
        int qty,
        long unitPriceCents
) {
}
