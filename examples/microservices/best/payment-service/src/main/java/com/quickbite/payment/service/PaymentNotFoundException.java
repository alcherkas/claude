package com.quickbite.payment.service;

/** Thrown when a payment id cannot be resolved. */
public class PaymentNotFoundException extends RuntimeException {
    public PaymentNotFoundException(String message) {
        super(message);
    }
}
