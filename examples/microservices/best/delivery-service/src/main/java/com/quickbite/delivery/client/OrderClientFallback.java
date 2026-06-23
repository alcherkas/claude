package com.quickbite.delivery.client;

import com.quickbite.delivery.dto.OrderSummary;
import com.quickbite.delivery.service.DependencyUnavailableException;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class OrderClientFallback implements OrderClient {

    @Override
    public OrderSummary getOrder(UUID id) {
        throw new DependencyUnavailableException("order-service is unavailable; cannot read order " + id);
    }
}
