package com.quickbite.menu.service;

/** Thrown when a referenced menu item does not exist. Maps to HTTP 404. */
public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}
