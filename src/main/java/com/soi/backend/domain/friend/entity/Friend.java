package com.soi.backend.domain.friend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter

public class Friend {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "requester_id")
    private Long requesterId;

    @Column(name = "receiver_id")
    private Long receiverId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private FriendStatus status;

    @Column(name = "requester_deleted")
    private Boolean requesterDeleted;

    @Column(name = "receiver_deleted")
    private Boolean receiverDeleted;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public Friend(Long requesterId, Long receiverId, FriendStatus status) {
        this.requesterId = requesterId;
        this.receiverId = receiverId;
        this.status = status;
        this.requesterDeleted = false;
        this.receiverDeleted = false;
        this.createdAt = LocalDateTime.now();
    }

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    public void SetFriendStatus(FriendStatus status) {
        this.status = status;
    }
    public void SetRequesterDeleted(Boolean requesterDeleted) {
        this.requesterDeleted = requesterDeleted;
    }
    public void SetReceiverDeleted(Boolean receiverDeleted) {
        this.receiverDeleted = receiverDeleted;
    }
}
