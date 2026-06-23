package com.quickbite.review.dto;

import java.util.UUID;

/**
 * Shape returned by identity-service GET /internal/users/{id}.
 * Only the fields review-service needs to confirm the reviewing user is a real, active account.
 */
public record UserSummary(
        UUID id,
        String email,
        String fullName,
        String role,
        boolean active
) {
}
