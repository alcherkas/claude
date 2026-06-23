package com.quickbite.pricing.client;

import com.quickbite.pricing.config.FeignConfig;
import com.quickbite.pricing.dto.PromotionApplyRequest;
import com.quickbite.pricing.dto.PromotionApplyResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Feign client to promotion-service (port 8087). Reserves a redemption of a promo
 * code against a subtotal via the target's internal endpoint. URL comes from
 * {@code clients.promotion.url}.
 */
@FeignClient(
        name = "promotion-service",
        url = "${clients.promotion.url}",
        configuration = FeignConfig.class,
        fallbackFactory = PromotionClientFallbackFactory.class
)
public interface PromotionClient {

    @PostMapping("/internal/promotions/apply")
    PromotionApplyResponse apply(@RequestBody PromotionApplyRequest request);
}
