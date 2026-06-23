package com.quickbite.driver.client;

import com.quickbite.driver.config.FeignConfig;
import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Feign client targeting identity-service's internal API to validate that a user
 * exists, is active and carries the requested role (COURIER for driver
 * registration). Cross-service calls only ever hit the dependency's /internal/**.
 */
@FeignClient(
        name = "identity",
        url = "${clients.identity.url}",
        configuration = FeignConfig.class,
        fallback = IdentityClientFallback.class
)
public interface IdentityClient {

    @GetMapping("/internal/users/{id}/validate")
    UserValidationResponse validate(@PathVariable("id") UUID id,
                                    @RequestParam("role") String role);
}
