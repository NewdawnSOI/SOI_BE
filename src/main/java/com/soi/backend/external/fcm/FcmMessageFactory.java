package com.soi.backend.external.fcm;

import com.google.firebase.messaging.ApnsConfig;
import com.google.firebase.messaging.Aps;
import com.google.firebase.messaging.MulticastMessage;
import com.soi.backend.domain.notification.dto.NotificationSendPayloadDto;
import com.soi.backend.domain.notification.entity.DevicePlatform;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FcmMessageFactory {

    public MulticastMessage create(DevicePlatform platform, List<String> tokens, NotificationSendPayloadDto payload) {
        MulticastMessage.Builder builder = MulticastMessage.builder()
                .addAllTokens(tokens)
                .putAllData(payload.getData());

        if (platform == DevicePlatform.IOS) {
            builder.setApnsConfig(createBackgroundApnsConfig());
        }

        return builder.build();
    }

    private ApnsConfig createBackgroundApnsConfig() {
        return ApnsConfig.builder()
                .putHeader("apns-push-type", "background")
                .putHeader("apns-priority", "5")
                .setAps(Aps.builder().setContentAvailable(true).build())
                .build();
    }
}
