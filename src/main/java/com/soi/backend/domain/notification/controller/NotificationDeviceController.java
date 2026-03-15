package com.soi.backend.domain.notification.controller;

import com.soi.backend.domain.notification.dto.NotificationDeleteTokenReqDto;
import com.soi.backend.domain.notification.dto.NotificationRegisterTokenReqDto;
import com.soi.backend.domain.notification.service.NotificationDeviceTokenService;
import com.soi.backend.global.ApiResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/notification/device-token")
@Tag(name = "notification device API", description = "FCM 디바이스 토큰 API")
public class NotificationDeviceController {

    private final NotificationDeviceTokenService notificationDeviceTokenService;

    @Operation(summary = "FCM 토큰 등록", description = "로그인 또는 앱 시작 시 발급된 FCM 토큰을 등록합니다.")
    @PostMapping("/register")
    public ResponseEntity<ApiResponseDto<Boolean>> register(
            @AuthenticationPrincipal Long userId,
            @RequestBody NotificationRegisterTokenReqDto requestDto
    ) {
        notificationDeviceTokenService.register(userId, requestDto);
        return ResponseEntity.ok(ApiResponseDto.success(true, "FCM 토큰 등록 완료"));
    }

    @Operation(summary = "FCM 토큰 삭제", description = "로그아웃 또는 토큰 만료 시 FCM 토큰을 비활성화합니다.")
    @PostMapping("/delete")
    public ResponseEntity<ApiResponseDto<Boolean>> delete(
            @AuthenticationPrincipal Long userId,
            @RequestBody NotificationDeleteTokenReqDto requestDto
    ) {
        notificationDeviceTokenService.delete(userId, requestDto);
        return ResponseEntity.ok(ApiResponseDto.success(true, "FCM 토큰 삭제 완료"));
    }
}
