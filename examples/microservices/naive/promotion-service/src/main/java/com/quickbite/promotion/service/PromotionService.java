package com.quickbite.promotion.service;

import com.quickbite.promotion.client.IdentityClient;
import com.quickbite.promotion.domain.Promotion;
import com.quickbite.promotion.domain.PromotionType;
import com.quickbite.promotion.domain.Redemption;
import com.quickbite.promotion.dto.ApplyPromotionRequest;
import com.quickbite.promotion.dto.ApplyPromotionResponse;
import com.quickbite.promotion.dto.CreatePromotionRequest;
import com.quickbite.promotion.dto.PromotionResponse;
import com.quickbite.promotion.dto.UserSummary;
import com.quickbite.promotion.dto.ValidationResponse;
import com.quickbite.promotion.repository.PromotionRepository;
import com.quickbite.promotion.repository.RedemptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PromotionService {

    private final PromotionRepository promotionRepository;
    private final RedemptionRepository redemptionRepository;
    private final IdentityClient identityClient;

    @Transactional
    public PromotionResponse create(CreatePromotionRequest req) {
        if (promotionRepository.existsByCodeIgnoreCase(req.code())) {
            throw PromotionException.conflict("Promotion code already exists: " + req.code());
        }
        if (!req.validTo().isAfter(req.validFrom())) {
            throw PromotionException.unprocessable("validTo must be after validFrom");
        }
        if (req.type() == PromotionType.PERCENT && (req.value() < 0 || req.value() > 100)) {
            throw PromotionException.unprocessable("PERCENT value must be between 0 and 100");
        }

        Promotion promotion = Promotion.builder()
                .code(req.code().trim())
                .type(req.type())
                .value(req.value())
                .minSubtotalCents(req.minSubtotalCents())
                .validFrom(req.validFrom())
                .validTo(req.validTo())
                .maxRedemptions(req.maxRedemptions())
                .perUserLimit(req.perUserLimit())
                .active(req.active() == null || req.active())
                .build();

        Promotion saved = promotionRepository.save(promotion);
        log.info("Created promotion id={} code={}", saved.getId(), saved.getCode());
        return PromotionResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<PromotionResponse> list() {
        return promotionRepository.findAll().stream()
                .map(PromotionResponse::from)
                .toList();
    }

    /**
     * Read-only validation: does the code apply for this subtotal/user right now,
     * and what discount would it yield. Does NOT reserve a redemption.
     */
    @Transactional(readOnly = true)
    public ValidationResponse validate(String code, long subtotalCents, Long userId) {
        Promotion promotion = promotionRepository.findByCodeIgnoreCase(code).orElse(null);
        if (promotion == null) {
            return ValidationResponse.invalid("Unknown promo code");
        }
        String rejection = evaluateEligibility(promotion, subtotalCents, userId);
        if (rejection != null) {
            return ValidationResponse.invalid(rejection);
        }
        long discount = computeDiscount(promotion, subtotalCents);
        return ValidationResponse.valid(promotion.getType(), discount);
    }

    /**
     * Reserves a redemption for the given order. Idempotent per (promotion, order):
     * a repeated apply for the same order returns the existing reservation's discount.
     */
    @Transactional
    public ApplyPromotionResponse apply(ApplyPromotionRequest req) {
        Promotion promotion = promotionRepository.findByCodeIgnoreCase(req.code())
                .orElseThrow(() -> PromotionException.notFound("Unknown promo code: " + req.code()));

        if (redemptionRepository.existsByPromotionIdAndOrderId(promotion.getId(), req.orderId())) {
            long existingDiscount = computeDiscount(promotion, req.subtotalCents());
            log.info("Promotion {} already reserved for order {}, returning existing discount {}",
                    promotion.getCode(), req.orderId(), existingDiscount);
            return new ApplyPromotionResponse(null, promotion.getType(), existingDiscount);
        }

        String rejection = evaluateEligibility(promotion, req.subtotalCents(), req.userId());
        if (rejection != null) {
            throw PromotionException.unprocessable(rejection);
        }

        long discount = computeDiscount(promotion, req.subtotalCents());
        Redemption redemption = redemptionRepository.save(Redemption.builder()
                .promotionId(promotion.getId())
                .userId(req.userId())
                .orderId(req.orderId())
                .redeemedAt(Instant.now())
                .build());

        log.info("Reserved redemption id={} promotion={} user={} order={} discount={}",
                redemption.getId(), promotion.getCode(), req.userId(), req.orderId(), discount);
        return new ApplyPromotionResponse(redemption.getId(), promotion.getType(), discount);
    }

    /**
     * Returns {@code null} when eligible, otherwise a human-readable rejection reason.
     */
    private String evaluateEligibility(Promotion p, long subtotalCents, Long userId) {
        if (!p.isActive()) {
            return "Promotion is not active";
        }
        Instant now = Instant.now();
        if (now.isBefore(p.getValidFrom())) {
            return "Promotion is not yet valid";
        }
        if (now.isAfter(p.getValidTo())) {
            return "Promotion has expired";
        }
        if (subtotalCents < p.getMinSubtotalCents()) {
            return "Subtotal below minimum of " + p.getMinSubtotalCents() + " cents";
        }
        if (p.getMaxRedemptions() != null
                && redemptionRepository.countByPromotionId(p.getId()) >= p.getMaxRedemptions()) {
            return "Promotion redemption limit reached";
        }
        if (userId != null && p.getPerUserLimit() != null
                && redemptionRepository.countByPromotionIdAndUserId(p.getId(), userId) >= p.getPerUserLimit()) {
            return "Per-user redemption limit reached";
        }
        if (userId != null && !userExists(userId)) {
            return "Unknown user";
        }
        return null;
    }

    /**
     * Optional identity targeting: if identity is reachable and explicitly reports the
     * user is missing, reject. If identity is down (fallback returns null) we proceed,
     * treating the dependency as optional.
     */
    private boolean userExists(Long userId) {
        try {
            UserSummary summary = identityClient.getUser(userId);
            // null => fallback (identity unavailable) => do not block.
            return summary == null || summary.id() != null;
        } catch (Exception ex) {
            log.warn("Identity lookup failed for user {}: {}", userId, ex.getMessage());
            return true;
        }
    }

    private long computeDiscount(Promotion p, long subtotalCents) {
        return switch (p.getType()) {
            case PERCENT -> Math.min(subtotalCents, Math.round(subtotalCents * (p.getValue() / 100.0)));
            case FIXED -> Math.min(subtotalCents, p.getValue());
            case FREE_DELIVERY -> 0L; // delivery fee is waived downstream by pricing-service
        };
    }
}
