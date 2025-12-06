package com.soi.backend.domain.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor

public class NotificationRespDto {
    private String text; // 알림 내용
    private String userProfile; // 알림이랑 관련된 유저 프로필이미지
    private String imageUrl; // 내용이랑 관련된 사진 이미지, 사진 없으면 그냥 null
    private Long relatedId; // 뭐든 관련된거의 id
}
