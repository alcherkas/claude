package com.quickbite.cart.web;

/**
 * Thrown when a cart operation conflicts with cart state — e.g. adding an item
 * from a different restaurant, or an item that is unavailable. Maps to HTTP 409.
 */
public class CartConflictException extends RuntimeException {
    public CartConflictException(String message) {
        super(message);
    }
}
