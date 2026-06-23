package com.quickbite.wallet.client;

/**
 * Shape returned by identity-service {@code GET /internal/users/{id}/validate}.
 */
public record UserValidationResponse(boolean valid) {
}
