package com.quickbite.order.event;

import com.quickbite.order.domain.Order;
import com.quickbite.order.domain.OrderStatus;

import java.util.UUID;

public record OrderStatusChangedPayload(
        UUID orderId,
        UUID userId,
        UUID restaurantId,
        OrderStatus oldStatus,
        OrderStatus newStatus
) {
    public static OrderStatusChangedPayload from(Order order, OrderStatus oldStatus) {
        return new OrderStatusChangedPayload(
                order.getId(),
                order.getUserId(),
                order.getRestaurantId(),
                oldStatus,
                order.getStatus());
    }
}
