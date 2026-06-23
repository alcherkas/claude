package com.quickbite.notification.web;

import com.quickbite.notification.dto.NotificationListResponse;
import com.quickbite.notification.dto.NotificationResponse;
import com.quickbite.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Internal service-to-service API under {@code /internal/**}. Never exposed through the gateway
 * (PLATFORM_SPEC §4). Lets sibling services inspect what was sent to a given user (e.g. for
 * support tooling or re-send decisions) without touching the public surface.
 */
@Tag(name = "Internal Notifications", description = "Service-to-service notification lookup (not gateway-exposed).")
@RestController
@RequestMapping("/internal/notifications")
@RequiredArgsConstructor
public class InternalNotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "Internal: list notifications delivered to a user")
    @GetMapping("/users/{userId}")
    public NotificationListResponse byUser(@PathVariable("userId") UUID userId) {
        List<NotificationResponse> items = notificationService.findForUser(userId).stream()
                .map(NotificationResponse::from)
                .toList();
        return new NotificationListResponse(userId, items.size(), items);
    }
}
