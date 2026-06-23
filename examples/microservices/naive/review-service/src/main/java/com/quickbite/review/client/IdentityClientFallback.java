package com.quickbite.review.client;

import com.quickbite.review.dto.UserSummary;
import com.quickbite.review.service.DependencyUnavailableException;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class IdentityClientFallback implements IdentityClient {

    @Override
    public UserSummary getUser(UUID id) {
        throw new DependencyUnavailableException("identity-service is unavailable; cannot verify user " + id);
    }
}
