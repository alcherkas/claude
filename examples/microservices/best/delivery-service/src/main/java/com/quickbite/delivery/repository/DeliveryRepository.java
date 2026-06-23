package com.quickbite.delivery.repository;

import com.quickbite.delivery.domain.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DeliveryRepository extends JpaRepository<Delivery, UUID> {

    List<Delivery> findByOrderId(UUID orderId);

    Optional<Delivery> findFirstByOrderId(UUID orderId);

    boolean existsByOrderId(UUID orderId);
}
