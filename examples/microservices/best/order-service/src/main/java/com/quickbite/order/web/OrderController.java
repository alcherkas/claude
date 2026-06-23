package com.quickbite.order.web;

import com.quickbite.order.domain.Order;
import com.quickbite.order.dto.CreateOrderRequest;
import com.quickbite.order.dto.OrderResponse;
import com.quickbite.order.dto.UpdateStatusRequest;
import com.quickbite.order.service.OrderMapper;
import com.quickbite.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/** Public order API exposed through the gateway at /api/orders/**. */
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final OrderMapper orderMapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse create(@Valid @RequestBody CreateOrderRequest request) {
        Order order = orderService.createOrder(request);
        return orderMapper.toResponse(order);
    }

    @GetMapping("/{id}")
    public OrderResponse get(@PathVariable UUID id) {
        return orderMapper.toResponse(orderService.getOrder(id));
    }

    @GetMapping
    public List<OrderResponse> listByUser(@RequestParam("userId") UUID userId) {
        return orderService.getOrdersForUser(userId).stream()
                .map(orderMapper::toResponse)
                .toList();
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<OrderResponse> updateStatus(@PathVariable UUID id,
                                                      @Valid @RequestBody UpdateStatusRequest request) {
        Order updated = orderService.updateStatus(id, request.status());
        return ResponseEntity.ok(orderMapper.toResponse(updated));
    }
}
