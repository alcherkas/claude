package com.quickbite.payment.service;

/** Thrown when a payment request is well-formed but not processable (e.g. wrong state to refund). */
public class PaymentValidationException extends RuntimeException {
    public PaymentValidationException(String message) {
        super(message);
    }
}
