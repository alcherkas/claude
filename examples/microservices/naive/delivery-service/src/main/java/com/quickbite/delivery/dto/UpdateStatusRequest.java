package com.quickbite.delivery.dto;

import com.quickbite.delivery.domain.DeliveryStatus;
import jakarta.validation.constraints.NotNull;

/** Public request body for PATCH /api/deliveries/{id}/status. */
public record UpdateStatusRequest(
        @NotNull DeliveryStatus status
) {
}
