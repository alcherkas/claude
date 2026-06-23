package com.quickbite.restaurant.client;

import java.util.UUID;

/**
 * Shape returned by identity-service {@code GET /internal/users/{id}/validate}.
 */
public record UserValidationResponse(
        UUID id,
        boolean valid,
        String role
) {
}
