package com.quickbite.cart.dto;

import java.time.Instant;
import java.util.List;

public record CartResponse(
        Long userId,
        Long restaurantId,
        List<CartItemResponse> items,
        long subtotalCents,
        Instant updatedAt
) {
}
