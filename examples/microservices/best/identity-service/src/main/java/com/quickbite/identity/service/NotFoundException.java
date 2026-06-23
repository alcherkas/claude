package com.quickbite.identity.service;

/** Thrown when a requested user cannot be found. Maps to HTTP 404. */
public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}
