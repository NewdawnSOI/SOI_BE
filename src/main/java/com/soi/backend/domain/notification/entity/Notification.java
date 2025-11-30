package com.soi.backend.domain.notification.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor

public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "send_user_id", nullable = false)
    private Long requesterId;

    @Column(name = "receiver_user_id", nullable = false)
    private Long receiverId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private NotificationType type;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "friend_id")
    private Long friendId; // 친구 관계 목록 테이블 아이디

    @Column(name = "category_id")
    private Long categoryId; // 카테고리 관련된 알람일 때 카테고리 아이디

    @Column(name = "category_invite_id")
    private Long categoryInviteId; // 초대 ID, category_invite 테이블의 아이디임

    @Column(name = "comment_id")
    private Long commentId; // 댓글 알람일떄 댓글 ID

    @Column(name = "post_id")
    private Long postId; // 댓글달린 게시물의 ID

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "is_read")
    private Boolean isRead;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    // 친구요청 알림
    public Notification(Long requesterId, Long receiverId, NotificationType type, String title,
                        Long friendId, Long categoryId, Long categoryInviteId, Long commentId) {
        this.requesterId = requesterId;
        this.receiverId = receiverId;
        this.friendId = friendId;
        this.type = type;
        this.title = title;
        this.categoryId = categoryId;
        this.categoryInviteId = categoryInviteId;
        this.commentId = commentId;
        this.createdAt = LocalDateTime.now();
        this.isRead = false;
    }

    // 카테고리 초대 알림
}
