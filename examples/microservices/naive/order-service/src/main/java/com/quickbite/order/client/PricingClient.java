package com.quickbite.order.client;

import com.quickbite.order.dto.PricingQuoteRequest;
import com.quickbite.order.dto.PricingQuoteResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "pricingClient", url = "${clients.pricing.url}", fallback = PricingClientFallback.class)
public interface PricingClient {

    @PostMapping("/internal/pricing/quote")
    PricingQuoteResponse quote(@RequestBody PricingQuoteRequest request);
}
