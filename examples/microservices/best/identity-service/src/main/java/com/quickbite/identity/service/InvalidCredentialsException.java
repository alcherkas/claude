package com.quickbite.identity.service;

/** Thrown on login when email/password do not match an active account. Maps to HTTP 401. */
public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException(String message) {
        super(message);
    }
}
