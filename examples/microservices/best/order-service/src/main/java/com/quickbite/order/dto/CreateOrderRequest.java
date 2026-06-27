package com.quickbite.order.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/** Public request body for POST /api/orders. */
public record CreateOrderRequest(
        @NotNull UUID userId,
        String promoCode,
        Long tipCents
) {
}
