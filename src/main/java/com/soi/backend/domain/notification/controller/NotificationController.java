package com.soi.backend.domain.notification.controller;

import com.soi.backend.domain.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
@RequestMapping("/notification")

@Tag(name = "notification API", description = "알림 관련 API")
public class NotificationController {

    private final NotificationService notificationService;

}
