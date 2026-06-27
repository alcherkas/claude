package com.quickbite.order.service;

import com.quickbite.order.domain.Order;
import com.quickbite.order.domain.OrderItem;
import com.quickbite.order.dto.OrderItemResponse;
import com.quickbite.order.dto.OrderResponse;
import com.quickbite.order.dto.OrderSummaryResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OrderMapper {

    public OrderResponse toResponse(Order order) {
        List<OrderItemResponse> items = order.getItems().stream()
                .map(this::toItemResponse)
                .toList();
        return new OrderResponse(
                order.getId(),
                order.getUserId(),
                order.getRestaurantId(),
                order.getStatus(),
                order.getSubtotalCents(),
                order.getDeliveryFeeCents(),
                order.getServiceFeeCents(),
                order.getTaxCents(),
                order.getDiscountCents(),
                order.getTipCents(),
                order.getTotalCents(),
                order.getCurrency(),
                order.getCreatedAt(),
                items);
    }

    public OrderSummaryResponse toSummary(Order order) {
        return new OrderSummaryResponse(
                order.getId(),
                order.getUserId(),
                order.getRestaurantId(),
                order.getStatus(),
                order.getTipCents(),
                order.getTotalCents(),
                order.getCurrency());
    }

    private OrderItemResponse toItemResponse(OrderItem item) {
        return new OrderItemResponse(
                item.getId(),
                item.getMenuItemId(),
                item.getName(),
                item.getQty(),
                item.getUnitPriceCents());
    }
}
