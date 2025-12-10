package com.soi.backend.domain.notification.controller;

import com.soi.backend.domain.notification.dto.NotificationGetAllRespDto;
import com.soi.backend.domain.notification.dto.NotificationRespDto;
import com.soi.backend.domain.notification.service.NotificationService;
import com.soi.backend.global.ApiResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("/notification")

@Tag(name = "notification API", description = "알림 관련 API")
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "알림 조회", description = "알림들을 조회합니다.")
    @PostMapping("/get-all")
    public ResponseEntity<ApiResponseDto<NotificationGetAllRespDto>> getAll(Long userId, int page) {
        NotificationGetAllRespDto notificationGetAllRespDto = notificationService.getAllNotifications(userId, page);
        return ResponseEntity.ok(ApiResponseDto.success(notificationGetAllRespDto, "모든 알림 조회 완료"));
    }

    @Operation(summary = "친구관련 알림 조회", description = "친구 요청 알림들을 조회합니다.")
    @PostMapping("/get-friend")
    public ResponseEntity<ApiResponseDto<List<NotificationRespDto>>> getFriend(Long userId, int page) {
        List<NotificationRespDto> notificationRespDtos = notificationService.getAllFriendNotifications(userId, page);
        return ResponseEntity.ok(ApiResponseDto.success(notificationRespDtos, "모든 알림 조회 완료"));
    }
}
