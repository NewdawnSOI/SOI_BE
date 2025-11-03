package com.soi.backend.domain.notification.service;

import com.soi.backend.domain.notification.entity.Notification;
import com.soi.backend.domain.notification.entity.NotificationType;
import com.soi.backend.domain.notification.repository.NotificationRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j

public class NotificationService {

    private final NotificationRepository notificationRepository;

    @Transactional
    public Long createNofication(Long friendId, Long senderId, Long receiverId,
                                 NotificationType type, String title) {
        Notification notification = new Notification(friendId, senderId, receiverId, type, title);
        notificationRepository.save(notification);
        return notification.getId();
    }
}
