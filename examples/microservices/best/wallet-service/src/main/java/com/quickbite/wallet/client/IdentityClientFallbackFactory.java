package com.quickbite.wallet.client;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Resilience4j fallback for {@link IdentityClient}. Identity is an optional guard
 * for wallet creation: a genuine 404 means the user does not exist (invalid), while
 * any other failure (circuit open, timeout, 5xx) is treated as "unknown but allowed"
 * so a transient identity outage does not block wallet credits. The fallback is
 * resilient and never throws.
 */
@Slf4j
@Component
public class IdentityClientFallbackFactory implements FallbackFactory<IdentityClient> {

    @Override
    public IdentityClient create(Throwable cause) {
        return id -> {
            if (cause instanceof FeignException.NotFound) {
                return new UserValidationResponse(false);
            }
            log.warn("identity-service unavailable while validating user {}: {}", id, cause.toString());
            // Fail open: allow the wallet operation to proceed when identity cannot be reached.
            return new UserValidationResponse(true);
        };
    }
}
