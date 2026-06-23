package com.quickbite.payment.domain;

/** Lifecycle of a payment. */
public enum PaymentStatus {
    AUTHORIZED,
    CAPTURED,
    REFUNDED,
    FAILED
}
