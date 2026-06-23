package com.quickbite.driver.client;

import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resilience4j fallback for {@link IdentityClient}. When identity-service is
 * unavailable we fail closed: the user is reported invalid so that driver
 * registration is rejected rather than silently trusting unverified input.
 */
@Slf4j
@Component
public class IdentityClientFallback implements IdentityClient {

    @Override
    public UserValidationResponse validate(UUID id, String role) {
        log.warn("IdentityClient.validate fallback triggered for user {} role {}; failing closed", id, role);
        return new UserValidationResponse(false);
    }
}
