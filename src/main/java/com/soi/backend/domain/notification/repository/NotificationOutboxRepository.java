package com.soi.backend.domain.notification.repository;

import com.soi.backend.domain.notification.entity.NotificationOutbox;
import com.soi.backend.domain.notification.entity.NotificationOutboxStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface NotificationOutboxRepository extends JpaRepository<NotificationOutbox, Long> {

    List<NotificationOutbox> findAllByStatusOrderByCreatedAtAsc(NotificationOutboxStatus status, Pageable pageable);

    List<NotificationOutbox> findAllByStatusAndNextRetryAtLessThanEqualOrderByCreatedAtAsc(
            NotificationOutboxStatus status,
            LocalDateTime nextRetryAt,
            Pageable pageable
    );
}
