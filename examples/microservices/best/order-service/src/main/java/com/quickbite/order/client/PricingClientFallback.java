package com.quickbite.order.client;

import com.quickbite.order.dto.PricingQuoteRequest;
import com.quickbite.order.dto.PricingQuoteResponse;
import com.quickbite.order.service.DependencyUnavailableException;
import org.springframework.stereotype.Component;

@Component
public class PricingClientFallback implements PricingClient {

    @Override
    public PricingQuoteResponse quote(PricingQuoteRequest request) {
        throw new DependencyUnavailableException("pricing-service is unavailable; cannot compute quote");
    }
}
