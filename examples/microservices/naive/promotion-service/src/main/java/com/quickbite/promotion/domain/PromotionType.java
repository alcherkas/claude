package com.quickbite.promotion.domain;

/**
 * Kind of discount a promotion applies.
 * <ul>
 *   <li>{@code PERCENT} — {@code value} is a percentage (0-100) of the subtotal.</li>
 *   <li>{@code FIXED} — {@code value} is a fixed amount in cents.</li>
 *   <li>{@code FREE_DELIVERY} — flags a waived delivery fee; {@code value} is ignored.</li>
 * </ul>
 */
public enum PromotionType {
    PERCENT,
    FIXED,
    FREE_DELIVERY
}
