package com.quickbite.wallet.web;

/**
 * Thrown when a debit would drive a wallet balance below zero. Maps to HTTP 409
 * so payment-service can reject the wallet payment cleanly.
 */
public class InsufficientFundsException extends RuntimeException {
    public InsufficientFundsException(String message) {
        super(message);
    }
}
