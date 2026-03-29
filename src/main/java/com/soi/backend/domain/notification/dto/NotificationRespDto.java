package com.soi.backend.domain.notification.dto;

import com.soi.backend.domain.notification.entity.NotificationType;
import com.soi.backend.domain.user.dto.NotificationUserRespDto;
import jakarta.annotation.Nonnull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor

public class NotificationRespDto {
    private Long id;
    private String text; // 알림 내용
    private String name;
    private String nickname;
    private String userProfileKey;
    private String userProfileUrl; // 알림이랑 관련된 유저 프로필이미지
    private String imageUrl; // 내용이랑 관련된 사진 이미지, 사진 없으면 그냥 null
    private NotificationType type;
    private Boolean isRead;
    private Long categoryIdForPost; // 게시물 알림일때 넣을 카테고리 id
    private Long relatedId; // 뭐든 관련된거의 id
    private Long replyCommentId; // 대댓글
    private Long parentCommentId; // 원댓글
    private List<NotificationUserRespDto> categoryInvitedUsers;
}
