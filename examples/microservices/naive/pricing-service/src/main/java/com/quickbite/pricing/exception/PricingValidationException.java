package com.quickbite.pricing.exception;

/**
 * Raised when a quote cannot be priced because of invalid input that only
 * becomes apparent after resolving upstream data (e.g. an item belongs to a
 * different restaurant, or is unavailable). Maps to HTTP 422.
 */
public class PricingValidationException extends RuntimeException {

    public PricingValidationException(String message) {
        super(message);
    }
}
