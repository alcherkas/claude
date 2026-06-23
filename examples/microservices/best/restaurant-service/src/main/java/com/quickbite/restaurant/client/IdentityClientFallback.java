package com.quickbite.restaurant.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Resilience4j fallback for {@link IdentityClient}. When identity-service is
 * unavailable we fail closed: the user is reported invalid with no role so that
 * restaurant creation is rejected rather than silently trusting unverified input.
 */
@Slf4j
@Component
public class IdentityClientFallback implements IdentityClient {

    @Override
    public UserValidationResponse validate(UUID id) {
        log.warn("IdentityClient.validate fallback triggered for user {}; failing closed", id);
        return new UserValidationResponse(id, false, null);
    }
}
