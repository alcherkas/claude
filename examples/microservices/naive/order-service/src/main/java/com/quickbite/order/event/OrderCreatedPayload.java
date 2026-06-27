package com.quickbite.order.event;

import com.quickbite.order.domain.Order;

import java.util.UUID;

public record OrderCreatedPayload(
        UUID orderId,
        UUID userId,
        UUID restaurantId,
        long tipCents,
        long totalCents,
        String currency
) {
    public static OrderCreatedPayload from(Order order) {
        return new OrderCreatedPayload(
                order.getId(),
                order.getUserId(),
                order.getRestaurantId(),
                order.getTipCents(),
                order.getTotalCents(),
                order.getCurrency());
    }
}
