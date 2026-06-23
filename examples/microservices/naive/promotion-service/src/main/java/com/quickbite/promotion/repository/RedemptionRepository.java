package com.quickbite.promotion.repository;

import com.quickbite.promotion.domain.Redemption;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RedemptionRepository extends JpaRepository<Redemption, Long> {

    long countByPromotionId(Long promotionId);

    long countByPromotionIdAndUserId(Long promotionId, Long userId);

    boolean existsByPromotionIdAndOrderId(Long promotionId, Long orderId);
}
