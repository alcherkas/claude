package com.quickbite.cart.dto;

/**
 * Result of validating a user against identity-service
 * GET /internal/users/{id}/validate. A cart may only be built for a user that
 * exists and is active.
 */
public record UserValidation(boolean valid) {
}
