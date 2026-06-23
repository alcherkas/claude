package com.quickbite.payment.client;

import com.quickbite.payment.dto.OrderSummary;
import com.quickbite.payment.service.DependencyUnavailableException;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class OrderClientFallback implements OrderClient {

    @Override
    public OrderSummary getOrder(UUID id) {
        throw new DependencyUnavailableException("order-service is unavailable; cannot resolve order amount");
    }
}
