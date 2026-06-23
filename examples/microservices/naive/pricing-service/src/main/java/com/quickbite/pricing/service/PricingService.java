package com.quickbite.pricing.service;

import com.quickbite.pricing.client.MenuClient;
import com.quickbite.pricing.client.PromotionClient;
import com.quickbite.pricing.dto.MenuItemView;
import com.quickbite.pricing.dto.PromotionApplyRequest;
import com.quickbite.pricing.dto.PromotionApplyResponse;
import com.quickbite.pricing.dto.QuoteRequest;
import com.quickbite.pricing.dto.QuoteResponse;
import com.quickbite.pricing.exception.PricingValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Stateless quote engine. Resolves authoritative prices from menu-service,
 * applies an optional promotion via promotion-service, and layers on the
 * platform's delivery, service and tax fees.
 *
 * <p>Fee policy (cents, integer arithmetic throughout):</p>
 * <ul>
 *     <li>delivery fee — flat 299 (waived when the promotion grants free delivery)</li>
 *     <li>service fee — 10% of subtotal, capped at 500</li>
 *     <li>tax — 8% of the taxable base (subtotal − discount, floored at 0)</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PricingService {

    static final long FLAT_DELIVERY_FEE_CENTS = 299L;
    static final long SERVICE_FEE_RATE_PERCENT = 10L;
    static final long SERVICE_FEE_CAP_CENTS = 500L;
    static final long TAX_RATE_PERCENT = 8L;
    static final String DEFAULT_CURRENCY = "USD";

    private final MenuClient menuClient;
    private final PromotionClient promotionClient;

    public QuoteResponse quote(QuoteRequest request) {
        List<QuoteResponse.LineItem> lineItems = new ArrayList<>();
        long subtotalCents = 0L;
        String currency = DEFAULT_CURRENCY;

        for (QuoteRequest.QuoteItem item : request.items()) {
            MenuItemView menuItem = menuClient.getMenuItem(item.menuItemId());
            if (menuItem == null) {
                throw new PricingValidationException("menu item " + item.menuItemId() + " not found");
            }
            if (!request.restaurantId().equals(menuItem.restaurantId())) {
                throw new PricingValidationException("menu item " + menuItem.id()
                        + " does not belong to restaurant " + request.restaurantId());
            }
            if (!menuItem.available()) {
                throw new PricingValidationException("menu item " + menuItem.id() + " is unavailable");
            }
            if (StringUtils.hasText(menuItem.currency())) {
                currency = menuItem.currency();
            }

            long lineTotal = menuItem.priceCents() * item.qty();
            subtotalCents += lineTotal;
            lineItems.add(new QuoteResponse.LineItem(
                    menuItem.id(),
                    menuItem.name(),
                    item.qty(),
                    menuItem.priceCents(),
                    lineTotal));
        }

        PromotionApplyResponse promo = applyPromotion(request, subtotalCents);
        long discountCents = Math.min(promo.discountCents(), subtotalCents);

        long deliveryFeeCents = promo.freeDelivery() ? 0L : FLAT_DELIVERY_FEE_CENTS;
        long serviceFeeCents = Math.min(
                subtotalCents * SERVICE_FEE_RATE_PERCENT / 100L, SERVICE_FEE_CAP_CENTS);

        long taxableBase = Math.max(0L, subtotalCents - discountCents);
        long taxCents = taxableBase * TAX_RATE_PERCENT / 100L;

        long totalCents = taxableBase + deliveryFeeCents + serviceFeeCents + taxCents;

        return new QuoteResponse(
                subtotalCents,
                deliveryFeeCents,
                serviceFeeCents,
                taxCents,
                discountCents,
                totalCents,
                currency,
                lineItems);
    }

    private PromotionApplyResponse applyPromotion(QuoteRequest request, long subtotalCents) {
        if (!StringUtils.hasText(request.promoCode())) {
            return PromotionApplyResponse.none();
        }
        PromotionApplyResponse response = promotionClient.apply(new PromotionApplyRequest(
                request.promoCode(),
                request.userId(),
                request.restaurantId(),
                subtotalCents));
        if (response == null || !response.valid()) {
            log.info("promo code '{}' rejected: {}", request.promoCode(),
                    response == null ? "null response" : response.reason());
            return PromotionApplyResponse.none();
        }
        return response;
    }
}
