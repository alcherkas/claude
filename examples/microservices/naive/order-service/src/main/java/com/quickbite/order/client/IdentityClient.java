package com.quickbite.order.client;

import com.quickbite.order.dto.UserSummary;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "identityClient", url = "${clients.identity.url}", fallback = IdentityClientFallback.class)
public interface IdentityClient {

    @GetMapping("/internal/users/{id}")
    UserSummary getUser(@PathVariable("id") UUID id);
}
