package com.quickbite.notification.domain;

/**
 * Lifecycle of a notification (PLATFORM_SPEC §3).
 */
public enum NotificationStatus {
    PENDING,
    SENT,
    FAILED
}
