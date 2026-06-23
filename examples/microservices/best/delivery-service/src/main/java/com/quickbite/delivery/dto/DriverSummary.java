package com.quickbite.delivery.dto;

import java.util.UUID;

/**
 * Mirror of driver-service's internal driver representation returned by
 * {@code GET /internal/drivers/available} and {@code POST /internal/drivers/{id}/assign}.
 */
public record DriverSummary(
        UUID id,
        UUID userId,
        String name,
        String vehicle,
        String status
) {
}
