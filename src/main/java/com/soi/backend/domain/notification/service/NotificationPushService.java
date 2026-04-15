package com.soi.backend.domain.notification.service;

import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.MessagingErrorCode;
import com.google.firebase.messaging.SendResponse;
import com.soi.backend.domain.notification.dto.NotificationSendPayloadDto;
import com.soi.backend.domain.notification.entity.DevicePlatform;
import com.soi.backend.domain.notification.entity.Notification;
import com.soi.backend.domain.notification.entity.NotificationOutbox;
import com.soi.backend.domain.notification.entity.UserDeviceToken;
import com.soi.backend.domain.notification.repository.NotificationRepository;
import com.soi.backend.domain.notification.repository.UserDeviceTokenRepository;
import com.soi.backend.external.fcm.FcmClient;
import com.soi.backend.global.metrics.BusinessMetricsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    private final BusinessMetricsService businessMetricsService;

    public void sendPending(int batchSize) {
        FcmClient fcmClient = fcmClientProvider.getIfAvailable();
        if (fcmClient == null) {
            log.warn("FCM client bean이 없어 outbox 발송을 건너뜁니다.");
            return;
        }

        List<NotificationOutbox> dispatchTargets = notificationOutboxService.findDispatchTargets(batchSize);
        if (dispatchTargets.isEmpty()) {
            return;
        }

        log.info("알림 outbox 발송 시작. count={}, batchSize={}", dispatchTargets.size(), batchSize);

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

        log.info("FCM 대상 조회 완료. outboxId={}, receiverId={}, targets={}, platforms={}",
                outbox.getId(),
                outbox.getReceiverId(),
                deviceTokens.size(),
                summarizePlatforms(deviceTokens));

        NotificationSendPayloadDto payload = notificationMessageFactory.create(notification);
        Map<DevicePlatform, List<UserDeviceToken>> tokensByPlatform = deviceTokens.stream()
                .collect(Collectors.groupingBy(
                        UserDeviceToken::getPlatform,
                        () -> new EnumMap<>(DevicePlatform.class),
                        Collectors.toList()
                ));

        boolean hasSuccess = false;
        String failureMessage = "FCM 발송 실패";

        for (Map.Entry<DevicePlatform, List<UserDeviceToken>> entry : tokensByPlatform.entrySet()) {
            DevicePlatform platform = entry.getKey();
            List<UserDeviceToken> platformTokens = entry.getValue();
            List<String> tokens = platformTokens.stream()
                    .map(UserDeviceToken::getToken)
                    .toList();

            BatchResponse batchResponse = fcmClient.sendMulticast(platform, tokens, payload);

            logBatchResponses(outbox, platformTokens, batchResponse);
            disableInvalidTokens(platformTokens, batchResponse);

            if (batchResponse.getSuccessCount() > 0) {
                hasSuccess = true;
            } else {
                failureMessage = extractFailureMessage(batchResponse);
            }
        }

        if (hasSuccess) {
            notificationOutboxService.markSent(outbox.getId());
            return;
        }

        notificationOutboxService.markRetry(outbox.getId(), failureMessage);
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

    private void logBatchResponses(
            NotificationOutbox outbox,
            List<UserDeviceToken> deviceTokens,
            BatchResponse batchResponse
    ) {
        List<SendResponse> responses = batchResponse.getResponses();
        for (int i = 0; i < responses.size(); i++) {
            SendResponse response = responses.get(i);
            UserDeviceToken deviceToken = deviceTokens.get(i);

            if (response.isSuccessful()) {
                businessMetricsService.increment(
                        "notification_push",
                        "platform", deviceToken.getPlatform().name(),
                        "status", "success"
                );
                log.info("FCM token 발송 성공. outboxId={}, platform={}, tokenSuffix={}",
                        outbox.getId(),
                        deviceToken.getPlatform(),
                        maskToken(deviceToken.getToken()));
                continue;
            }

            String errorCode = response.getException() == null || response.getException().getMessagingErrorCode() == null
                    ? "UNKNOWN"
                    : response.getException().getMessagingErrorCode().name();
            String errorMessage = response.getException() == null
                    ? "unknown error"
                    : response.getException().getMessage();

            businessMetricsService.increment(
                    "notification_push",
                    "platform", deviceToken.getPlatform().name(),
                    "status", "failure",
                    "error_code", errorCode
            );
            log.warn("FCM token 발송 실패. outboxId={}, platform={}, tokenSuffix={}, errorCode={}, message={}",
                    outbox.getId(),
                    deviceToken.getPlatform(),
                    maskToken(deviceToken.getToken()),
                    errorCode,
                    errorMessage);
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

    private Map<DevicePlatform, Integer> summarizePlatforms(List<UserDeviceToken> deviceTokens) {
        Map<DevicePlatform, Integer> platformCounts = new EnumMap<>(DevicePlatform.class);
        for (UserDeviceToken deviceToken : deviceTokens) {
            platformCounts.merge(deviceToken.getPlatform(), 1, Integer::sum);
        }
        return platformCounts;
    }

    private String maskToken(String token) {
        if (token == null || token.isBlank()) {
            return "empty";
        }

        int start = Math.max(0, token.length() - 8);
        return token.substring(start);
    }
}
