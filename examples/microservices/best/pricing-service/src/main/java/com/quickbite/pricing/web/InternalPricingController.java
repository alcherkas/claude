package com.quickbite.pricing.web;

import com.quickbite.pricing.dto.QuoteRequest;
import com.quickbite.pricing.dto.QuoteResponse;
import com.quickbite.pricing.service.PricingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Internal, service-to-service pricing API under {@code /internal/**}. This is
 * never exposed through the gateway. order-service calls this during checkout to
 * obtain the authoritative pricing snapshot it persists with the order.
 */
@RestController
@RequestMapping("/internal/pricing")
@RequiredArgsConstructor
@Tag(name = "Internal Pricing", description = "Service-to-service quote endpoint (not gateway-exposed)")
public class InternalPricingController {

    private final PricingService pricingService;

    @PostMapping("/quote")
    @Operation(summary = "Price a cart (internal)",
            description = "Identical pricing logic to the public endpoint; consumed by order-service.")
    public QuoteResponse quote(@Valid @RequestBody QuoteRequest request) {
        return pricingService.quote(request);
    }
}
