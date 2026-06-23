package com.quickbite.cart.web;

/** Thrown when a required dependency (menu-service) is unreachable. Maps to HTTP 503. */
public class UpstreamUnavailableException extends RuntimeException {
    public UpstreamUnavailableException(String message) {
        super(message);
    }
}
