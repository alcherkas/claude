package com.quickbite.notification.web;

import com.quickbite.notification.domain.Channel;
import com.quickbite.notification.domain.Notification;
import com.quickbite.notification.domain.NotificationStatus;
import com.quickbite.notification.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * MockMvc slice test for the public {@code GET /api/notifications} endpoint. The service layer is
 * mocked so the test exercises request mapping, parameter binding and JSON serialization only.
 */
@WebMvcTest(NotificationController.class)
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotificationService notificationService;

    @Test
    void list_returnsUserNotifications() throws Exception {
        UUID userId = UUID.randomUUID();
        Notification n = Notification.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .channel(Channel.EMAIL)
                .template("order.created")
                .payloadJson("Hi customer, your order #1234abcd has been placed for 24.50 USD.")
                .status(NotificationStatus.SENT)
                .createdAt(Instant.parse("2026-06-22T10:00:00Z"))
                .sentAt(Instant.parse("2026-06-22T10:00:01Z"))
                .build();
        when(notificationService.findForUser(eq(userId))).thenReturn(List.of(n));

        mockMvc.perform(get("/api/notifications").param("userId", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(1))
                .andExpect(jsonPath("$.notifications[0].template").value("order.created"))
                .andExpect(jsonPath("$.notifications[0].channel").value("EMAIL"))
                .andExpect(jsonPath("$.notifications[0].status").value("SENT"));
    }

    @Test
    void list_missingUserId_returns400() throws Exception {
        mockMvc.perform(get("/api/notifications"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.path").value("/api/notifications"));
    }
}
