package com.soi.backend.domain.user.dto;

import com.soi.backend.domain.notification.dto.NotificationRespDto;
import com.soi.backend.domain.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter

public class NotificationUserRespDto {
    private Long id;
    private String nickname;
    private String name;
    private String profileImageKey;

    public static NotificationUserRespDto toDto(User user) {
        return new  NotificationUserRespDto(
                user.getId(),
                user.getNickname(),
                user.getName(),
                user.getProfileImageKey()
        );
    }

    public static List<NotificationUserRespDto> toDto(List<User> users) {
        return users.stream()
                .map(NotificationUserRespDto::toDto)
                .toList();
    }
}
