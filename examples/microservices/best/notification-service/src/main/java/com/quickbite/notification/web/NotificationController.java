package com.quickbite.notification.web;

import com.quickbite.notification.dto.NotificationListResponse;
import com.quickbite.notification.dto.NotificationResponse;
import com.quickbite.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Public notification history API, exposed through the gateway at {@code /api/notifications/**}.
 */
@Tag(name = "Notifications", description = "Read a user's notification history.")
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "List notifications for a user, newest first")
    @GetMapping
    public NotificationListResponse list(@RequestParam("userId") UUID userId) {
        List<NotificationResponse> items = notificationService.findForUser(userId).stream()
                .map(NotificationResponse::from)
                .toList();
        return new NotificationListResponse(userId, items.size(), items);
    }
}
