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
    public Long createNotification(Long requesterId, Long receiverId, NotificationType type, String title,
                                 Long friendId, Long categoryId, Long categoryInviteId, Long commentId) {
        Notification notification = new Notification(requesterId, receiverId, type, title, friendId, categoryId, categoryInviteId, commentId);
        notificationRepository.save(notification);
        return notification.getId();
    }

    @Transactional
    public Long createFriendNotification(Long requesterId, Long receiverId, NotificationType type,
                                                    String title, Long friendId) {
        return createNotification(requesterId, receiverId, type, title, friendId, null, null, null);
    }

    @Transactional
    public Long createCategoryNotification(Long requesterId, Long receiverId, NotificationType type,
                                           String title, Long categoryId, Long categoryInviteId) {
        return createNotification(requesterId, receiverId, type, title, null, categoryId, categoryInviteId, null);
    }

}
