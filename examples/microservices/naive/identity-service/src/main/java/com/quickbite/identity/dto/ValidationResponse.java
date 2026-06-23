package com.quickbite.identity.dto;

/**
 * Response for {@code GET /internal/users/{id}/validate?role=ROLE}.
 */
public record ValidationResponse(boolean valid) {
}
