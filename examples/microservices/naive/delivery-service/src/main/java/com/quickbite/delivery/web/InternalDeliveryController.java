package com.quickbite.delivery.web;

import com.quickbite.delivery.dto.DeliveryResponse;
import com.quickbite.delivery.service.DeliveryMapper;
import com.quickbite.delivery.service.DeliveryNotFoundException;
import com.quickbite.delivery.service.DeliveryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Internal service-to-service API. Never exposed through the gateway.
 * Lets other services (e.g. notification, order) look up a delivery by id or by order.
 */
@RestController
@RequestMapping("/internal/deliveries")
@RequiredArgsConstructor
public class InternalDeliveryController {

    private final DeliveryService deliveryService;
    private final DeliveryMapper deliveryMapper;

    @GetMapping("/{id}")
    public DeliveryResponse getById(@PathVariable UUID id) {
        return deliveryMapper.toResponse(deliveryService.getDelivery(id));
    }

    @GetMapping
    public DeliveryResponse getByOrder(@RequestParam("orderId") UUID orderId) {
        return deliveryService.getDeliveriesForOrder(orderId).stream()
                .findFirst()
                .map(deliveryMapper::toResponse)
                .orElseThrow(() -> new DeliveryNotFoundException("No delivery for order " + orderId));
    }
}
