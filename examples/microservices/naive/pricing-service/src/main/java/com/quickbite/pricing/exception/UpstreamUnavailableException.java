package com.quickbite.pricing.exception;

/**
 * Raised when a required upstream dependency (e.g. menu-service) cannot be
 * reached and pricing cannot proceed. Maps to HTTP 503.
 */
public class UpstreamUnavailableException extends RuntimeException {

    public UpstreamUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
