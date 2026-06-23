package com.quickbite.promotion.service;

import org.springframework.http.HttpStatus;

/**
 * Domain exception carrying an HTTP status for the global error handler to surface.
 */
public class PromotionException extends RuntimeException {

    private final HttpStatus status;

    public PromotionException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public static PromotionException notFound(String message) {
        return new PromotionException(HttpStatus.NOT_FOUND, message);
    }

    public static PromotionException conflict(String message) {
        return new PromotionException(HttpStatus.CONFLICT, message);
    }

    public static PromotionException unprocessable(String message) {
        return new PromotionException(HttpStatus.UNPROCESSABLE_ENTITY, message);
    }
}
