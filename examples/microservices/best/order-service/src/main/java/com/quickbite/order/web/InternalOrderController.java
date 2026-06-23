package com.quickbite.order.web;

import com.quickbite.order.dto.OrderSummaryResponse;
import com.quickbite.order.service.OrderMapper;
import com.quickbite.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Internal service-to-service API. Never exposed through the gateway.
 * Consumed by payment-service, delivery-service and review-service.
 */
@RestController
@RequestMapping("/internal/orders")
@RequiredArgsConstructor
public class InternalOrderController {

    private final OrderService orderService;
    private final OrderMapper orderMapper;

    @GetMapping("/{id}")
    public OrderSummaryResponse getSummary(@PathVariable UUID id) {
        return orderMapper.toSummary(orderService.getOrder(id));
    }
}
