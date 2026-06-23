package com.quickbite.driver.dto;

import com.quickbite.driver.domain.Vehicle;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/**
 * Body of {@code POST /api/drivers}. The {@code userId} must reference an
 * identity-service user carrying the COURIER role.
 */
public record CreateDriverRequest(
        @NotNull(message = "is required") UUID userId,
        @NotBlank(message = "is required") String name,
        @NotNull(message = "is required") Vehicle vehicle
) {
}
