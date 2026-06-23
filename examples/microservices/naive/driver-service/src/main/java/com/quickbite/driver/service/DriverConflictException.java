package com.quickbite.driver.service;

/**
 * Raised on illegal state transitions, e.g. registering the same courier twice
 * or assigning a driver that is not currently AVAILABLE.
 */
public class DriverConflictException extends RuntimeException {
    public DriverConflictException(String message) {
        super(message);
    }
}
