package com.quickbite.delivery.service;

import com.quickbite.delivery.client.DriverClient;
import com.quickbite.delivery.client.OrderClient;
import com.quickbite.delivery.domain.Delivery;
import com.quickbite.delivery.domain.DeliveryStatus;
import com.quickbite.delivery.domain.TrackingPoint;
import com.quickbite.delivery.dto.DriverSummary;
import com.quickbite.delivery.dto.OrderSummary;
import com.quickbite.delivery.event.DeliveryEventProducer;
import com.quickbite.delivery.repository.DeliveryRepository;
import com.quickbite.delivery.repository.TrackingPointRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final TrackingPointRepository trackingPointRepository;
    private final OrderClient orderClient;
    private final DriverClient driverClient;
    private final DeliveryEventProducer eventProducer;

    /**
     * Public create flow (POST /api/deliveries):
     * 1) read the order via order-service /internal
     * 2) find an AVAILABLE driver and assign it via driver-service /internal
     * 3) persist the delivery as ASSIGNED and emit DeliveryStatusChanged
     */
    @Transactional
    public Delivery createDelivery(UUID orderId) {
        OrderSummary order = orderClient.getOrder(orderId);
        if (order == null) {
            throw new DeliveryValidationException("Unknown order " + orderId);
        }
        if (deliveryRepository.existsByOrderId(orderId)) {
            throw new DeliveryValidationException("A delivery already exists for order " + orderId);
        }

        DriverSummary available = driverClient.findAvailable();
        if (available == null) {
            throw new DeliveryValidationException("No available driver to dispatch for order " + orderId);
        }
        DriverSummary assigned = driverClient.assign(available.id());

        Delivery delivery = Delivery.builder()
                .id(UUID.randomUUID())
                .orderId(orderId)
                .userId(order.userId())
                .driverId(assigned.id())
                .status(DeliveryStatus.ASSIGNED)
                .createdAt(Instant.now())
                .build();

        Delivery saved = deliveryRepository.save(delivery);
        eventProducer.publishStatusChanged(saved, DeliveryStatus.PENDING);
        log.info("Created delivery {} for order {} assigned to driver {}",
                saved.getId(), orderId, assigned.id());
        return saved;
    }

    /**
     * Event-driven create (Kafka): an order became READY. Creates a PENDING delivery if one
     * does not already exist. Idempotent so a redelivered event does not duplicate work.
     */
    @Transactional
    public void createPendingForReadyOrder(UUID orderId) {
        if (deliveryRepository.existsByOrderId(orderId)) {
            log.debug("Delivery already exists for order {}; ignoring READY event", orderId);
            return;
        }
        // Resolve the customer so DeliveryStatusChanged carries a recipient for notification-service.
        OrderSummary order = orderClient.getOrder(orderId);
        UUID userId = order != null ? order.userId() : null;
        Delivery delivery = Delivery.builder()
                .id(UUID.randomUUID())
                .orderId(orderId)
                .userId(userId)
                .status(DeliveryStatus.PENDING)
                .createdAt(Instant.now())
                .build();
        Delivery saved = deliveryRepository.save(delivery);
        eventProducer.publishStatusChanged(saved, DeliveryStatus.PENDING);
        log.info("Auto-created PENDING delivery {} for READY order {}", saved.getId(), orderId);
    }

    @Transactional(readOnly = true)
    public Delivery getDelivery(UUID id) {
        return deliveryRepository.findById(id)
                .orElseThrow(() -> new DeliveryNotFoundException("Delivery " + id + " not found"));
    }

    @Transactional(readOnly = true)
    public List<Delivery> getDeliveriesForOrder(UUID orderId) {
        return deliveryRepository.findByOrderId(orderId);
    }

    @Transactional(readOnly = true)
    public List<TrackingPoint> getTracking(UUID deliveryId) {
        // Ensure the delivery exists before returning its (possibly empty) trail.
        getDelivery(deliveryId);
        return trackingPointRepository.findByDeliveryIdOrderByAtAsc(deliveryId);
    }

    /** Public status update (PATCH /api/deliveries/{id}/status). Emits DeliveryStatusChanged. */
    @Transactional
    public Delivery updateStatus(UUID id, DeliveryStatus newStatus) {
        Delivery delivery = getDelivery(id);
        DeliveryStatus old = delivery.getStatus();
        if (old == newStatus) {
            return delivery;
        }
        delivery.setStatus(newStatus);
        Delivery saved = deliveryRepository.save(delivery);
        eventProducer.publishStatusChanged(saved, old);
        log.info("Delivery {} transitioned {} -> {}", id, old, newStatus);
        return saved;
    }
}
