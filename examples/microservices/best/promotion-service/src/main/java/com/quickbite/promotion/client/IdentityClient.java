package com.quickbite.promotion.client;

import com.quickbite.promotion.config.FeignConfig;
import com.quickbite.promotion.dto.UserSummary;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Optional sync dependency on identity-service. Used to confirm that a userId
 * presented during validation/apply corresponds to a real user before a redemption
 * is reserved. Targets identity's {@code /internal/**} endpoints only.
 */
@FeignClient(
        name = "identity",
        url = "${clients.identity.url}",
        path = "/internal",
        configuration = FeignConfig.class,
        fallback = IdentityClientFallback.class
)
public interface IdentityClient {

    @GetMapping("/users/{id}")
    UserSummary getUser(@PathVariable("id") Long id);
}
