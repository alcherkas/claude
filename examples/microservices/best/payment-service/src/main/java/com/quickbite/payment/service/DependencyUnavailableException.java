package com.quickbite.payment.service;

/** Thrown by Feign fallbacks when a downstream dependency is unreachable. */
public class DependencyUnavailableException extends RuntimeException {
    public DependencyUnavailableException(String message) {
        super(message);
    }
}
