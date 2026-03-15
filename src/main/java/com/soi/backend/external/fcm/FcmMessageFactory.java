package com.soi.backend.external.fcm;

import com.google.firebase.messaging.MulticastMessage;
import com.soi.backend.domain.notification.dto.NotificationSendPayloadDto;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FcmMessageFactory {

    public MulticastMessage create(List<String> tokens, NotificationSendPayloadDto payload) {
        MulticastMessage.Builder builder = MulticastMessage.builder()
                .addAllTokens(tokens)
                .putAllData(payload.getData());

        if (payload.getTitle() != null && payload.getBody() != null) {
            builder.setNotification(com.google.firebase.messaging.Notification.builder()
                    .setTitle(payload.getTitle())
                    .setBody(payload.getBody())
                    .build());
        }

        return builder.build();
    }
}
