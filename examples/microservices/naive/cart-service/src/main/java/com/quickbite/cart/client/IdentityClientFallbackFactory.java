package com.quickbite.cart.client;

import com.quickbite.cart.dto.UserValidation;
import com.quickbite.cart.web.NotFoundException;
import com.quickbite.cart.web.UpstreamUnavailableException;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

/**
 * Resilience4j fallback for {@link IdentityClient}. A genuine 404 from
 * identity-service means the user does not exist; any other failure (circuit
 * open, timeout, 5xx) is surfaced as upstream-unavailable so the cart operation
 * fails closed.
 */
@Slf4j
@Component
public class IdentityClientFallbackFactory implements FallbackFactory<IdentityClient> {

    @Override
    public IdentityClient create(Throwable cause) {
        return id -> {
            if (cause instanceof FeignException.NotFound) {
                throw new NotFoundException("User " + id + " does not exist");
            }
            log.warn("identity-service unavailable while validating user {}: {}", id, cause.toString());
            throw new UpstreamUnavailableException("identity-service is unavailable; cannot validate user " + id);
        };
    }
}
