package com.quickbite.pricing.client;

import com.quickbite.pricing.dto.PromotionApplyRequest;
import com.quickbite.pricing.dto.PromotionApplyResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

/**
 * Resilience4j fallback for {@link PromotionClient}. Promotions are non-critical:
 * if promotion-service is down we degrade gracefully and price the cart with no
 * discount rather than failing the whole quote.
 */
@Slf4j
@Component
public class PromotionClientFallbackFactory implements FallbackFactory<PromotionClient> {

    @Override
    public PromotionClient create(Throwable cause) {
        return request -> {
            log.warn("promotion-service unavailable, pricing without discount for code '{}': {}",
                    request.code(), cause.toString());
            return PromotionApplyResponse.none();
        };
    }
}
