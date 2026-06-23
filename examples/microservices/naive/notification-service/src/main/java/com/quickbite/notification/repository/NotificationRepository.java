package com.quickbite.notification.repository;

import com.quickbite.notification.domain.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    /** A user's notifications, newest first — backs {@code GET /api/notifications?userId=}. */
    List<Notification> findByUserIdOrderByCreatedAtDesc(@Param("userId") UUID userId);
}
