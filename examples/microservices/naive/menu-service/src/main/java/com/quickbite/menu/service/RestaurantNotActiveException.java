package com.quickbite.menu.service;

/**
 * Thrown when the owning restaurant is missing or not ACTIVE. Maps to HTTP 422.
 */
public class RestaurantNotActiveException extends RuntimeException {
    public RestaurantNotActiveException(String message) {
        super(message);
    }
}
