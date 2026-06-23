package com.quickbite.notification.dto;

import com.quickbite.notification.domain.Channel;
import com.quickbite.notification.domain.Notification;
import com.quickbite.notification.domain.NotificationStatus;

import java.time.Instant;
import java.util.UUID;

/**
 * Public view of a {@link Notification} returned by {@code GET /api/notifications}.
 */
public record NotificationResponse(
        UUID id,
        UUID userId,
        Channel channel,
        String template,
        String message,
        NotificationStatus status,
        Instant createdAt,
        Instant sentAt
) {
    public static NotificationResponse from(Notification n) {
        return new NotificationResponse(
                n.getId(),
                n.getUserId(),
                n.getChannel(),
                n.getTemplate(),
                n.getPayloadJson(),
                n.getStatus(),
                n.getCreatedAt(),
                n.getSentAt());
    }
}
