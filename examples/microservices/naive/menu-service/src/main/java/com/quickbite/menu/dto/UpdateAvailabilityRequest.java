package com.quickbite.menu.dto;

import jakarta.validation.constraints.NotNull;

/**
 * Payload for PATCH /api/menu/{id}/availability.
 */
public record UpdateAvailabilityRequest(
        @NotNull Boolean available
) {
}
