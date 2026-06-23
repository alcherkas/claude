package com.quickbite.review.client;

import com.quickbite.review.dto.OrderSummary;
import com.quickbite.review.service.DependencyUnavailableException;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class OrderClientFallback implements OrderClient {

    @Override
    public OrderSummary getOrder(UUID id) {
        throw new DependencyUnavailableException("order-service is unavailable; cannot verify order " + id);
    }
}
