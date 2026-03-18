package com.soi.backend.domain.notification.dto;

import com.soi.backend.domain.notification.entity.Notification;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDataPayloadDto {

    private String notificationId;
    private String type;
    private String friendId;
    private String categoryId;
    private String categoryInviteId;
    private String postId;
    private String commentId;
    private String nickname;
    private String body;
    private String imageUrl;

    public static NotificationDataPayloadDto from(
            Notification notification,
            String nickname,
            String body,
            String imageUrl
    ) {
        return NotificationDataPayloadDto.builder()
                .notificationId(stringify(notification.getId()))
                .type(notification.getType().name())
                .friendId(stringify(notification.getFriendId()))
                .categoryId(stringify(notification.getCategoryId()))
                .categoryInviteId(stringify(notification.getCategoryInviteId()))
                .postId(stringify(notification.getPostId()))
                .commentId(stringify(notification.getCommentId()))
                .nickname(nickname)
                .body(body)
                .imageUrl(imageUrl)
                .build();
    }

    public Map<String, String> toMap() {
        Map<String, String> data = new LinkedHashMap<>();
        putIfPresent(data, "notificationId", notificationId);
        putIfPresent(data, "type", type);
        putIfPresent(data, "friendId", friendId);
        putIfPresent(data, "categoryId", categoryId);
        putIfPresent(data, "categoryInviteId", categoryInviteId);
        putIfPresent(data, "postId", postId);
        putIfPresent(data, "commentId", commentId);
        putIfPresent(data, "nickname", nickname);
        putIfPresent(data, "body", body);
        putIfPresent(data, "imageUrl", imageUrl);
        return data;
    }

    private static void putIfPresent(Map<String, String> target, String key, String value) {
        if (value != null && !value.isBlank()) {
            target.put(key, value);
        }
    }

    private static String stringify(Long value) {
        return value == null ? null : String.valueOf(value);
    }
}
