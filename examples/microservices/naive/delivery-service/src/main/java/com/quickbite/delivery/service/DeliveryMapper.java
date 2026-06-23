package com.quickbite.delivery.service;

import com.quickbite.delivery.domain.Delivery;
import com.quickbite.delivery.domain.TrackingPoint;
import com.quickbite.delivery.dto.DeliveryResponse;
import com.quickbite.delivery.dto.TrackingPointResponse;
import org.springframework.stereotype.Component;

@Component
public class DeliveryMapper {

    public DeliveryResponse toResponse(Delivery delivery) {
        return new DeliveryResponse(
                delivery.getId(),
                delivery.getOrderId(),
                delivery.getDriverId(),
                delivery.getStatus(),
                delivery.getCreatedAt());
    }

    public TrackingPointResponse toTrackingResponse(TrackingPoint point) {
        return new TrackingPointResponse(
                point.getId(),
                point.getLat(),
                point.getLng(),
                point.getAt());
    }
}
