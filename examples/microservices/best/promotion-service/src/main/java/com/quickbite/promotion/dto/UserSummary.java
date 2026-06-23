package com.quickbite.promotion.dto;

/**
 * Slim view of an identity user as returned by identity-service {@code /internal}.
 */
public record UserSummary(
        Long id,
        String email,
        String role
) {
}
