package com.soi.backend.domain.notification.scheduler;

import com.soi.backend.domain.notification.service.NotificationPushService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.fcm.enabled", havingValue = "true")
public class NotificationOutboxScheduler {

    private final NotificationPushService notificationPushService;

    @Value("${app.notification.outbox.batch-size:100}")
    private int batchSize;

    @Scheduled(fixedDelayString = "${app.notification.outbox.poll-interval-ms:3000}")
    public void sendPendingNotifications() {
        notificationPushService.sendPending(batchSize);
    }
}
