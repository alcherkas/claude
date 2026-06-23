package com.quickbite.order.client;

import com.quickbite.order.dto.CartSnapshot;
import com.quickbite.order.service.DependencyUnavailableException;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class CartClientFallback implements CartClient {

    @Override
    public CartSnapshot snapshot(UUID userId) {
        throw new DependencyUnavailableException("cart-service is unavailable; cannot load cart snapshot");
    }
}
