package com.soi.backend.domain.notification.dto;

import lombok.Getter;

import java.util.List;

@Getter

public class NotificationGetAllRespDto {
    private final Integer friendReqCount; // 친구요청온거 갯수
    private final List<NotificationRespDto> notifications;

    public NotificationGetAllRespDto(List<NotificationRespDto>  notifications) {
        this.friendReqCount = notifications.size();
        this.notifications = notifications;
    }
}
