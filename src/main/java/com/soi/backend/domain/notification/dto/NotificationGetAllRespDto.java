package com.soi.backend.domain.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor

public class NotificationGetAllRespDto {
    private final Long friendReqCount; // 친구요청온거 갯수
    private final List<NotificationRespDto> notifications;

    public NotificationGetAllRespDto(List<NotificationRespDto>  notifications, Long count) {
        this.friendReqCount = count;
        this.notifications = notifications;
    }
}
