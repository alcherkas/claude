package com.quickbite.order.client;

import com.quickbite.order.dto.UserSummary;
import com.quickbite.order.service.DependencyUnavailableException;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class IdentityClientFallback implements IdentityClient {

    @Override
    public UserSummary getUser(UUID id) {
        throw new DependencyUnavailableException("identity-service is unavailable; cannot validate user");
    }
}
