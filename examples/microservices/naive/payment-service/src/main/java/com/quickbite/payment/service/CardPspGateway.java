package com.quickbite.payment.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Mock card payment service provider (PSP). In production this would call Stripe/Adyen.
 * Here it deterministically "captures" any positive amount and returns a provider reference.
 */
@Slf4j
@Component
public class CardPspGateway {

    public static final String PROVIDER_NAME = "mock-card-psp";

    public record CaptureResult(boolean success, String reference) {
    }

    public CaptureResult capture(UUID orderId, long amountCents, String currency) {
        if (amountCents <= 0) {
            log.warn("Refusing card capture for non-positive amount {} on order {}", amountCents, orderId);
            return new CaptureResult(false, null);
        }
        String reference = "psp_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        log.info("Mock PSP captured {} {} for order {} -> {}", amountCents, currency, orderId, reference);
        return new CaptureResult(true, reference);
    }
}
