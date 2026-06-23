package com.quickbite.wallet.web;

/** Thrown when a wallet cannot be located. Maps to HTTP 404. */
public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}
