package com.quickbite.identity.dto;

import java.time.Instant;

/**
 * Response for {@code POST /api/auth/login}.
 */
public record LoginResponse(
        String token,
        Instant expiresAt,
        UserResponse user
) {
}
