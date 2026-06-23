package com.quickbite.review.client;

import com.quickbite.review.dto.UserSummary;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

/** Mandatory dependency: confirms the reviewing user exists and is active via identity /internal. */
@FeignClient(name = "identityClient", url = "${clients.identity.url}", fallback = IdentityClientFallback.class)
public interface IdentityClient {

    @GetMapping("/internal/users/{id}")
    UserSummary getUser(@PathVariable("id") UUID id);
}
