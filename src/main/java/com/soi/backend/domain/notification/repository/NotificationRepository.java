package com.soi.backend.domain.notification.repository;

import com.soi.backend.domain.notification.entity.Notification;
import com.soi.backend.domain.notification.entity.NotificationType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    Optional<Notification> findByFriendId(Long friendId);

    List<Notification> getAllByReceiverIdOrderByCreatedAtDesc(Long receiverId, Pageable pageable);

    @Query("""
        SELECT n.id
        FROM Notification n
        WHERE n.receiverId = :userId AND n.categoryId = :categoryId
    """)
    List<Long> findAllIdByCategoryId(Long userId, Long categoryId);

    @Query("""
        SELECT n.id
        FROM Notification n
        WHERE n.receiverId = :userId AND n.categoryInviteId = :categoryId
    """)
    List<Long> deleteCategoryInviteNotification(Long userId, Long categoryId);

    @Query("""
        SELECT n.id
        FROM Notification n
        WHERE n.receiverId = :userId AND n.postId = :postId
    """)
    List<Long> findAllIdByPostId(Long userId, Long postId);

    @Query("""
        SELECT n.id
        FROM Notification n
        WHERE n.receiverId = :userId AND n.commentId = :commentId
    """)
    List<Long> findAllIdByCommentId(Long userId, Long commentId);

    Long findByReceiverIdAndCategoryId(Long userId, Long categoryId);

    @Query("""
    SELECT COUNT(n)
    FROM Notification n
    WHERE n.receiverId = :userId
      AND n.type = :type
    """)
    long countByReceiverIdAndType(Long userId, NotificationType type);

}
