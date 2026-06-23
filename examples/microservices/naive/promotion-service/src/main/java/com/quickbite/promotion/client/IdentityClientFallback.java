package com.quickbite.promotion.client;

import com.quickbite.promotion.dto.UserSummary;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resilience4j fallback for {@link IdentityClient}. Identity is an optional dependency
 * for promotions, so when it is unreachable we degrade gracefully by returning
 * {@code null}, letting the caller treat the user as "unverified" rather than failing
 * the whole validation/apply flow.
 */
@Slf4j
@Component
public class IdentityClientFallback implements IdentityClient {

    @Override
    public UserSummary getUser(Long id) {
        log.warn("IdentityClient fallback: could not verify user {}; proceeding unverified", id);
        return null;
    }
}
