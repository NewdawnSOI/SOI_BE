package com.soi.backend.domain.notification.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "notification_outbox")
public class NotificationOutbox {

    private static final int MAX_RETRY_COUNT = 5;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "notification_id", nullable = false)
    private Long notificationId;

    @Column(name = "receiver_user_id", nullable = false)
    private Long receiverId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private NotificationOutboxStatus status;

    @Column(name = "retry_count", nullable = false)
    private int retryCount;

    @Column(name = "last_error", length = 1000)
    private String lastError;

    @Column(name = "next_retry_at")
    private LocalDateTime nextRetryAt;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public NotificationOutbox(Long notificationId, Long receiverId) {
        this.notificationId = notificationId;
        this.receiverId = receiverId;
        this.status = NotificationOutboxStatus.PENDING;
        this.retryCount = 0;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (this.createdAt == null) {
            this.createdAt = now;
        }
        this.updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void markSent() {
        this.status = NotificationOutboxStatus.SENT;
        this.sentAt = LocalDateTime.now();
        this.nextRetryAt = null;
        this.lastError = null;
    }

    public void markRetry(String errorMessage) {
        this.retryCount += 1;
        this.lastError = trimError(errorMessage);

        if (this.retryCount >= MAX_RETRY_COUNT) {
            this.status = NotificationOutboxStatus.FAILED;
            this.nextRetryAt = null;
            return;
        }

        this.status = NotificationOutboxStatus.RETRY;
        this.nextRetryAt = LocalDateTime.now().plusMinutes((long) this.retryCount * 5L);
    }

    public void markFailed(String errorMessage) {
        this.status = NotificationOutboxStatus.FAILED;
        this.lastError = trimError(errorMessage);
        this.nextRetryAt = null;
    }

    private String trimError(String errorMessage) {
        if (errorMessage == null) {
            return null;
        }

        if (errorMessage.length() <= 1000) {
            return errorMessage;
        }

        return errorMessage.substring(0, 1000);
    }
}
