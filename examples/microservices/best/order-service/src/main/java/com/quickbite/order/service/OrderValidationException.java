package com.quickbite.order.service;

/** Thrown for business-rule violations during order creation/transition. */
public class OrderValidationException extends RuntimeException {
    public OrderValidationException(String message) {
        super(message);
    }
}
