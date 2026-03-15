package com.soi.backend.domain.notification.entity;

public enum NotificationOutboxStatus {
    PENDING,
    RETRY,
    SENT,
    FAILED
}
