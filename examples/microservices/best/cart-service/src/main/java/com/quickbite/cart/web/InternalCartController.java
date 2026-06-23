package com.quickbite.cart.web;

import com.quickbite.cart.dto.CartSnapshot;
import com.quickbite.cart.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Service-to-service API. Never exposed through the gateway.
 * Consumed by order-service to load the cart snapshot during order creation.
 */
@RestController
@RequestMapping("/internal/carts")
@RequiredArgsConstructor
public class InternalCartController {

    private final CartService cartService;

    @GetMapping("/{userId}/snapshot")
    public CartSnapshot snapshot(@PathVariable Long userId) {
        return cartService.snapshot(userId);
    }
}
