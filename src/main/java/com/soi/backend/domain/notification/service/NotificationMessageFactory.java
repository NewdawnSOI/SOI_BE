package com.soi.backend.domain.notification.service;

import com.soi.backend.domain.comment.service.CommentReadService;
import com.soi.backend.domain.notification.dto.NotificationDataPayloadDto;
import com.soi.backend.domain.notification.dto.NotificationSendPayloadDto;
import com.soi.backend.domain.notification.entity.Notification;
import com.soi.backend.domain.notification.entity.NotificationType;
import com.soi.backend.domain.media.service.MediaService;
import com.soi.backend.domain.user.entity.User;
import com.soi.backend.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationMessageFactory {

    private final UserRepository userRepository;
    private final MediaService mediaService;
    private final CommentReadService commentReadService;

    public NotificationSendPayloadDto create(Notification notification) {
        User requester = notification.getRequesterId() == null
                ? null
                : userRepository.findById(notification.getRequesterId()).orElse(null);

        String nickname = requester == null ? null : requester.getNickname();
        String body = notification.getTitle();
        String imageUrl = resolveImageUrl(notification.getImageKey());
        Long parentCommentId = notification.getType() == NotificationType.COMMENT_REPLY_ADDED ? commentReadService.getParentCommentIdOfReply(notification.getCommentId()) : null;

        return NotificationSendPayloadDto.builder()
                .title(makeTitle(notification.getType()))
                .body(body)
                .data(NotificationDataPayloadDto.from(notification, nickname, body, imageUrl, parentCommentId).toMap())
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

    private String resolveImageUrl(String imageKey) {
        if (imageKey == null || imageKey.isBlank()) {
            return null;
        }

        return mediaService.getPresignedUrlByKey(imageKey);
    }
}
