package com.quickbite.delivery.web;

import com.quickbite.delivery.domain.Delivery;
import com.quickbite.delivery.dto.CreateDeliveryRequest;
import com.quickbite.delivery.dto.DeliveryResponse;
import com.quickbite.delivery.dto.TrackingPointResponse;
import com.quickbite.delivery.dto.UpdateStatusRequest;
import com.quickbite.delivery.service.DeliveryMapper;
import com.quickbite.delivery.service.DeliveryService;
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

/** Public delivery API exposed through the gateway at /api/deliveries/**. */
@RestController
@RequestMapping("/api/deliveries")
@RequiredArgsConstructor
public class DeliveryController {

    private final DeliveryService deliveryService;
    private final DeliveryMapper deliveryMapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DeliveryResponse create(@Valid @RequestBody CreateDeliveryRequest request) {
        Delivery delivery = deliveryService.createDelivery(request.orderId());
        return deliveryMapper.toResponse(delivery);
    }

    @GetMapping("/{id}")
    public DeliveryResponse get(@PathVariable UUID id) {
        return deliveryMapper.toResponse(deliveryService.getDelivery(id));
    }

    @GetMapping
    public List<DeliveryResponse> listByOrder(@RequestParam("orderId") UUID orderId) {
        return deliveryService.getDeliveriesForOrder(orderId).stream()
                .map(deliveryMapper::toResponse)
                .toList();
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<DeliveryResponse> updateStatus(@PathVariable UUID id,
                                                         @Valid @RequestBody UpdateStatusRequest request) {
        Delivery updated = deliveryService.updateStatus(id, request.status());
        return ResponseEntity.ok(deliveryMapper.toResponse(updated));
    }

    @GetMapping("/{id}/tracking")
    public List<TrackingPointResponse> tracking(@PathVariable UUID id) {
        return deliveryService.getTracking(id).stream()
                .map(deliveryMapper::toTrackingResponse)
                .toList();
    }
}
