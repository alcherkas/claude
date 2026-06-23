package com.quickbite.restaurant.client;

import com.quickbite.restaurant.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

/**
 * Feign client targeting identity-service's internal API to validate that an
 * owner user exists and carries the RESTAURANT_OWNER role.
 */
@FeignClient(
        name = "identity",
        url = "${clients.identity.url}",
        configuration = FeignConfig.class,
        fallback = IdentityClientFallback.class
)
public interface IdentityClient {

    @GetMapping("/internal/users/{id}/validate")
    UserValidationResponse validate(@PathVariable("id") UUID id);
}
