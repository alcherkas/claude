package com.quickbite.notification.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.quickbite.notification.domain.Channel;
import com.quickbite.notification.domain.Notification;
import com.quickbite.notification.domain.NotificationStatus;
import com.quickbite.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Core business logic: resolve the recipient, render the template, persist the notification and
 * "send" it (mock — logs). Also serves the read API.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository repository;
    private final TemplateRenderer renderer;

    /**
     * Handle one inbound event: render the message from the event payload, persist PENDING,
     * mock-send, then mark SENT/FAILED. Each event yields exactly one EMAIL notification in this
     * example. The recipient user id arrives on the event payload, so no synchronous lookup is needed.
     */
    @Transactional
    public Notification handleEvent(UUID userId, String template, String eventType, JsonNode payload) {
        String message = renderer.render(template, payload);

        Notification notification = Notification.builder()
                .userId(userId)
                .channel(Channel.EMAIL)
                .template(template)
                .payloadJson(message)
                .status(NotificationStatus.PENDING)
                .createdAt(Instant.now())
                .build();
        notification = repository.save(notification);

        try {
            mockSend(notification);
            notification.setStatus(NotificationStatus.SENT);
            notification.setSentAt(Instant.now());
        } catch (Exception ex) {
            log.error("Failed to send notification {} (event {}): {}",
                    notification.getId(), eventType, ex.getMessage());
            notification.setStatus(NotificationStatus.FAILED);
        }
        return repository.save(notification);
    }

    @Transactional(readOnly = true)
    public List<Notification> findForUser(UUID userId) {
        return repository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    /** Mock send: a real implementation would call SES/Twilio/FCM per {@link Channel}. */
    private void mockSend(Notification notification) {
        log.info("[MOCK-SEND] channel={} template={} to=user:{} :: {}",
                notification.getChannel(), notification.getTemplate(), notification.getUserId(),
                notification.getPayloadJson());
    }
}
