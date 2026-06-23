package com.quickbite.cart.web;

/** Thrown when a cart or cart item cannot be located. Maps to HTTP 404. */
public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}
