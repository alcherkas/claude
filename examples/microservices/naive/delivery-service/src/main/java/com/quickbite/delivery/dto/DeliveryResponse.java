package com.quickbite.delivery.dto;

import com.quickbite.delivery.domain.DeliveryStatus;

import java.time.Instant;
import java.util.UUID;

/** Public delivery representation returned by the /api/deliveries endpoints. */
public record DeliveryResponse(
        UUID id,
        UUID orderId,
        UUID driverId,
        DeliveryStatus status,
        Instant createdAt
) {
}
