package com.quickbite.delivery.repository;

import com.quickbite.delivery.domain.TrackingPoint;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TrackingPointRepository extends JpaRepository<TrackingPoint, UUID> {

    List<TrackingPoint> findByDeliveryIdOrderByAtAsc(UUID deliveryId);
}
