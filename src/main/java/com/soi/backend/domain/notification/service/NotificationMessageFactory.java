package com.soi.backend.domain.notification.service;

import com.soi.backend.domain.notification.dto.NotificationDataPayloadDto;
import com.soi.backend.domain.notification.dto.NotificationSendPayloadDto;
import com.soi.backend.domain.notification.entity.Notification;
import com.soi.backend.domain.notification.entity.NotificationType;
import org.springframework.stereotype.Component;

@Component
public class NotificationMessageFactory {

    public NotificationSendPayloadDto create(Notification notification) {
        return NotificationSendPayloadDto.builder()
                .title(makeTitle(notification.getType()))
                .body(notification.getTitle())
                .data(NotificationDataPayloadDto.from(notification).toMap())
                .build();
    }

    private String makeTitle(NotificationType type) {
        return switch (type) {
            case FRIEND_REQUEST, FRIEND_RESPOND -> "친구 알림";
            case CATEGORY_INVITE, CATEGORY_ADDED -> "카테고리 알림";
            case PHOTO_ADDED,
                 COMMENT_ADDED,
                 COMMENT_AUDIO_ADDED,
                 COMMENT_VIDEO_ADDED,
                 COMMENT_PHOTO_ADDED,
                 COMMENT_REPLY_ADDED -> "게시물 알림";
        };
    }
}
