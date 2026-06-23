package com.quickbite.identity.service;

/** Thrown on register when the email is already taken. Maps to HTTP 409. */
public class EmailAlreadyUsedException extends RuntimeException {
    public EmailAlreadyUsedException(String message) {
        super(message);
    }
}
