package com.soi.backend.domain.notification.dto;

import com.soi.backend.domain.notification.entity.DevicePlatform;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRegisterTokenReqDto {

    private String token;
    private DevicePlatform platform;
}
