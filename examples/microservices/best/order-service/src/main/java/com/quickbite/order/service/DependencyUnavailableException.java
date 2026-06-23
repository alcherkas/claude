package com.quickbite.order.service;

/** Thrown by Feign fallbacks when a required upstream dependency is unreachable. */
public class DependencyUnavailableException extends RuntimeException {
    public DependencyUnavailableException(String message) {
        super(message);
    }
}
