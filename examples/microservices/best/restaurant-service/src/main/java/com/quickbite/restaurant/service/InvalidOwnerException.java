package com.quickbite.restaurant.service;

import java.util.UUID;

/**
 * Raised when the supplied ownerUserId does not resolve to a valid
 * RESTAURANT_OWNER in identity-service.
 */
public class InvalidOwnerException extends RuntimeException {
    public InvalidOwnerException(UUID ownerUserId, String detail) {
        super("Invalid restaurant owner " + ownerUserId + ": " + detail);
    }
}
