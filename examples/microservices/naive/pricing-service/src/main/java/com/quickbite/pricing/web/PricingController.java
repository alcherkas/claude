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
 * Public pricing API, fronted by the gateway at {@code /api/pricing/**}.
 */
@RestController
@RequestMapping("/api/pricing")
@RequiredArgsConstructor
@Tag(name = "Pricing", description = "Compute an authoritative price quote for a cart")
public class PricingController {

    private final PricingService pricingService;

    @PostMapping("/quote")
    @Operation(summary = "Price a cart",
            description = "Fetches authoritative menu prices, applies an optional promotion, "
                    + "and returns the full fee breakdown.")
    public QuoteResponse quote(@Valid @RequestBody QuoteRequest request) {
        return pricingService.quote(request);
    }
}
