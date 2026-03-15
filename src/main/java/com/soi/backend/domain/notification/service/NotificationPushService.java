package com.soi.backend.domain.notification.service;

import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.MessagingErrorCode;
import com.google.firebase.messaging.SendResponse;
import com.soi.backend.domain.notification.dto.NotificationSendPayloadDto;
import com.soi.backend.domain.notification.entity.Notification;
import com.soi.backend.domain.notification.entity.NotificationOutbox;
import com.soi.backend.domain.notification.entity.UserDeviceToken;
import com.soi.backend.domain.notification.repository.NotificationRepository;
import com.soi.backend.domain.notification.repository.UserDeviceTokenRepository;
import com.soi.backend.external.fcm.FcmClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationPushService {

    private final NotificationRepository notificationRepository;
    private final UserDeviceTokenRepository userDeviceTokenRepository;
    private final NotificationOutboxService notificationOutboxService;
    private final NotificationMessageFactory notificationMessageFactory;
    private final NotificationDeviceTokenService notificationDeviceTokenService;
    private final ObjectProvider<FcmClient> fcmClientProvider;

    public void sendPending(int batchSize) {
        FcmClient fcmClient = fcmClientProvider.getIfAvailable();
        if (fcmClient == null) {
            log.warn("FCM client bean이 없어 outbox 발송을 건너뜁니다.");
            return;
        }

        List<NotificationOutbox> dispatchTargets = notificationOutboxService.findDispatchTargets(batchSize);
        for (NotificationOutbox outbox : dispatchTargets) {
            try {
                sendSingle(fcmClient, outbox);
            } catch (Exception e) {
                log.error("알림 push 발송 실패. outboxId={}, message={}", outbox.getId(), e.getMessage(), e);
                notificationOutboxService.markRetry(outbox.getId(), e.getMessage());
            }
        }
    }

    private void sendSingle(FcmClient fcmClient, NotificationOutbox outbox) throws FirebaseMessagingException {
        Notification notification = notificationRepository.findById(outbox.getNotificationId())
                .orElse(null);

        if (notification == null) {
            notificationOutboxService.markFailed(outbox.getId(), "원본 notification 데이터가 없습니다.");
            return;
        }

        List<UserDeviceToken> deviceTokens =
                userDeviceTokenRepository.findAllByUserIdAndEnabledTrue(outbox.getReceiverId());

        if (deviceTokens.isEmpty()) {
            notificationOutboxService.markSent(outbox.getId());
            return;
        }

        List<String> tokens = deviceTokens.stream()
                .map(UserDeviceToken::getToken)
                .toList();

        NotificationSendPayloadDto payload = notificationMessageFactory.create(notification);
        BatchResponse batchResponse = fcmClient.sendMulticast(tokens, payload);

        disableInvalidTokens(deviceTokens, batchResponse);

        if (batchResponse.getSuccessCount() > 0) {
            notificationOutboxService.markSent(outbox.getId());
            return;
        }

        notificationOutboxService.markRetry(outbox.getId(), extractFailureMessage(batchResponse));
    }

    private void disableInvalidTokens(List<UserDeviceToken> deviceTokens, BatchResponse batchResponse) {
        List<SendResponse> responses = batchResponse.getResponses();
        for (int i = 0; i < responses.size(); i++) {
            SendResponse response = responses.get(i);
            if (response.isSuccessful() || response.getException() == null) {
                continue;
            }

            MessagingErrorCode errorCode = response.getException().getMessagingErrorCode();
            if (errorCode == MessagingErrorCode.UNREGISTERED || errorCode == MessagingErrorCode.INVALID_ARGUMENT) {
                notificationDeviceTokenService.disable(deviceTokens.get(i).getToken());
            }
        }
    }

    private String extractFailureMessage(BatchResponse batchResponse) {
        for (SendResponse response : batchResponse.getResponses()) {
            if (!response.isSuccessful() && response.getException() != null) {
                return response.getException().getMessage();
            }
        }

        return "FCM 발송 실패";
    }
}
