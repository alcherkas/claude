package com.quickbite.driver.dto;

import com.quickbite.driver.domain.DriverStatus;
import jakarta.validation.constraints.NotNull;

/**
 * Body of {@code PATCH /api/drivers/{id}/availability}.
 */
public record UpdateAvailabilityRequest(
        @NotNull(message = "is required") DriverStatus status
) {
}
