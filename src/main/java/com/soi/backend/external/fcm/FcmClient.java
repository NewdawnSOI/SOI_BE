package com.soi.backend.external.fcm;

import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.soi.backend.domain.notification.dto.NotificationSendPayloadDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class FcmClient {

    private final FirebaseMessaging firebaseMessaging;
    private final FcmMessageFactory fcmMessageFactory;

    public BatchResponse sendMulticast(List<String> tokens, NotificationSendPayloadDto payload)
            throws FirebaseMessagingException {
        return firebaseMessaging.sendEachForMulticast(
                fcmMessageFactory.create(tokens, payload)
        );
    }
}
