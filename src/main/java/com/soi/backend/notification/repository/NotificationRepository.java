package com.soi.backend.notification.repository;

import com.soi.backend.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    Optional<Notification> findByFriendId(Long friendId);
}
