package com.quickbite.driver.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

/**
 * Body of {@code POST /api/drivers/{id}/location}.
 */
public record LocationPingRequest(
        @NotNull(message = "is required")
        @DecimalMin(value = "-90.0", message = "must be >= -90")
        @DecimalMax(value = "90.0", message = "must be <= 90")
        Double lat,

        @NotNull(message = "is required")
        @DecimalMin(value = "-180.0", message = "must be >= -180")
        @DecimalMax(value = "180.0", message = "must be <= 180")
        Double lng
) {
}
