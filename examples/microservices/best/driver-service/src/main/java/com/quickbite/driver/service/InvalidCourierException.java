package com.quickbite.driver.service;

import java.util.UUID;

/**
 * Raised when a user cannot be registered as a driver because identity-service
 * does not recognise them as an active COURIER.
 */
public class InvalidCourierException extends RuntimeException {
    public InvalidCourierException(UUID userId) {
        super("User " + userId + " is not a valid COURIER and cannot be registered as a driver");
    }
}
