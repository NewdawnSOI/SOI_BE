package com.soi.backend.domain.notification.dto;

import com.soi.backend.domain.notification.entity.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor

public class NotificationReqDto {
    private Long requesterId;
    private Long receiverId;
    private NotificationType type;
    private String title;

    // 선택적 필드
    private Long friendId;
    private Long categoryId;
    private Long categoryInviteId;
    private Long commentId;
    private Long postId;
    private String imageKey;
}
