package com.quickbite.notification.dto;

import java.util.List;
import java.util.UUID;

/**
 * Envelope for a user's notification history.
 */
public record NotificationListResponse(
        UUID userId,
        int count,
        List<NotificationResponse> notifications
) {
}
