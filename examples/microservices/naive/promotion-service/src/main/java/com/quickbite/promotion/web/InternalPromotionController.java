package com.quickbite.promotion.web;

import com.quickbite.promotion.dto.ApplyPromotionRequest;
import com.quickbite.promotion.dto.ApplyPromotionResponse;
import com.quickbite.promotion.service.PromotionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Service-to-service endpoints. Never exposed through the gateway.
 * Consumed by pricing-service and order-service to reserve a redemption.
 */
@RestController
@RequestMapping("/internal/promotions")
@RequiredArgsConstructor
public class InternalPromotionController {

    private final PromotionService promotionService;

    @PostMapping("/apply")
    public ApplyPromotionResponse apply(@Valid @RequestBody ApplyPromotionRequest request) {
        return promotionService.apply(request);
    }
}
