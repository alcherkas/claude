package com.quickbite.promotion.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "promotions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Promotion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private PromotionType type;

    /**
     * For {@link PromotionType#PERCENT} this is a whole percentage (e.g. 15 == 15%).
     * For {@link PromotionType#FIXED} this is an amount in cents.
     * For {@link PromotionType#FREE_DELIVERY} this is ignored.
     */
    @Column(nullable = false)
    private long value;

    @Column(name = "min_subtotal_cents", nullable = false)
    private long minSubtotalCents;

    @Column(name = "valid_from", nullable = false)
    private Instant validFrom;

    @Column(name = "valid_to", nullable = false)
    private Instant validTo;

    /** Total redemptions allowed across all users. {@code null} == unlimited. */
    @Column(name = "max_redemptions")
    private Integer maxRedemptions;

    /** Redemptions allowed per single user. {@code null} == unlimited. */
    @Column(name = "per_user_limit")
    private Integer perUserLimit;

    @Column(nullable = false)
    private boolean active;
}
