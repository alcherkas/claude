package com.quickbite.cart.client;

import com.quickbite.cart.config.FeignConfig;
import com.quickbite.cart.dto.UserValidation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Feign client to identity-service. Cart-service validates that the owning user
 * exists and is active before building a cart, via the dependency's /internal
 * endpoints.
 */
@FeignClient(
        name = "identity-service",
        url = "${clients.identity.url}",
        configuration = FeignConfig.class,
        fallbackFactory = IdentityClientFallbackFactory.class
)
public interface IdentityClient {

    @GetMapping("/internal/users/{id}/validate")
    UserValidation validateUser(@PathVariable("id") Long id);
}
