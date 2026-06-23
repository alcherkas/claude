package com.quickbite.order.dto;

import com.quickbite.order.domain.OrderStatus;
import jakarta.validation.constraints.NotNull;

/** Public request body for PATCH /api/orders/{id}/status. */
public record UpdateStatusRequest(
        @NotNull OrderStatus status
) {
}
