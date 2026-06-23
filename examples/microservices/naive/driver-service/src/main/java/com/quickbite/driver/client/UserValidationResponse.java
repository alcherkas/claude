package com.quickbite.driver.client;

/**
 * Shape returned by identity-service {@code GET /internal/users/{id}/validate?role=ROLE}.
 */
public record UserValidationResponse(boolean valid) {
}
