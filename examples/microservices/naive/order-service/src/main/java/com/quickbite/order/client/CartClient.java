package com.quickbite.order.client;

import com.quickbite.order.dto.CartSnapshot;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "cartClient", url = "${clients.cart.url}", fallback = CartClientFallback.class)
public interface CartClient {

    @GetMapping("/internal/carts/{userId}/snapshot")
    CartSnapshot snapshot(@PathVariable("userId") UUID userId);
}
