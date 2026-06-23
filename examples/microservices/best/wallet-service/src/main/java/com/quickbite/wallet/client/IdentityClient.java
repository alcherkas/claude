package com.quickbite.wallet.client;

import com.quickbite.wallet.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

/**
 * Feign client targeting identity-service's internal API to confirm that a user
 * exists before their wallet is created on first credit. Cross-service calls
 * only ever hit the dependency's /internal/** endpoints.
 */
@FeignClient(
        name = "identity-service",
        url = "${clients.identity.url}",
        configuration = FeignConfig.class,
        fallbackFactory = IdentityClientFallbackFactory.class
)
public interface IdentityClient {

    @GetMapping("/internal/users/{id}/validate")
    UserValidationResponse validate(@PathVariable("id") UUID id);
}
