package com.quickbite.delivery.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/** Public request body for POST /api/deliveries. */
public record CreateDeliveryRequest(
        @NotNull UUID orderId
) {
}
