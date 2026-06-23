package com.quickbite.order.dto;

import java.util.UUID;

/** Shape returned by identity-service GET /internal/users/{id}. */
public record UserSummary(
        UUID id,
        String email,
        String role
) {
}
