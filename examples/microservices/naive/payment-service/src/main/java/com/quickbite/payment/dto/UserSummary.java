package com.quickbite.payment.dto;

import java.util.UUID;

/** Mirror of identity-service's GET /internal/users/{id} response. */
public record UserSummary(
        UUID id,
        String email,
        String fullName,
        String role
) {
}
