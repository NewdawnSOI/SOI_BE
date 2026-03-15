package com.soi.backend.domain.notification.service;

import com.soi.backend.domain.notification.entity.NotificationOutbox;
import com.soi.backend.domain.notification.entity.NotificationOutboxStatus;
import com.soi.backend.domain.notification.repository.NotificationOutboxRepository;
import com.soi.backend.global.exception.CustomException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationOutboxService {

    private final NotificationOutboxRepository notificationOutboxRepository;

    @Transactional
    public void enqueue(Long notificationId, Long receiverId) {
        notificationOutboxRepository.save(new NotificationOutbox(notificationId, receiverId));
    }

    public List<NotificationOutbox> findDispatchTargets(int batchSize) {
        Pageable pendingPageable = PageRequest.of(0, batchSize);
        List<NotificationOutbox> pendingTargets = notificationOutboxRepository.findAllByStatusOrderByCreatedAtAsc(
                NotificationOutboxStatus.PENDING,
                pendingPageable
        );

        if (pendingTargets.size() >= batchSize) {
            return pendingTargets;
        }

        int remainSize = batchSize - pendingTargets.size();
        Pageable retryPageable = PageRequest.of(0, remainSize);
        List<NotificationOutbox> retryTargets =
                notificationOutboxRepository.findAllByStatusAndNextRetryAtLessThanEqualOrderByCreatedAtAsc(
                        NotificationOutboxStatus.RETRY,
                        LocalDateTime.now(),
                        retryPageable
                );

        List<NotificationOutbox> dispatchTargets = new ArrayList<>(pendingTargets);
        dispatchTargets.addAll(retryTargets);
        return dispatchTargets;
    }

    @Transactional
    public void markSent(Long outboxId) {
        findById(outboxId).markSent();
    }

    @Transactional
    public void markRetry(Long outboxId, String errorMessage) {
        findById(outboxId).markRetry(errorMessage);
    }

    @Transactional
    public void markFailed(Long outboxId, String errorMessage) {
        findById(outboxId).markFailed(errorMessage);
    }

    private NotificationOutbox findById(Long outboxId) {
        return notificationOutboxRepository.findById(outboxId)
                .orElseThrow(() -> new CustomException("알림 outbox를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
    }
}
