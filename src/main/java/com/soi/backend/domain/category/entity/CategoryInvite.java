package com.soi.backend.domain.category.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor

@Table(name = "category_invite", schema = "soi")
public class CategoryInvite {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "category_id")
    private Long categoryId;

    @Column(name = "inviter_user_id")
    private Long inviterUserId;

    @Column(name = "invited_user_id")
    private Long invitedUserId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private CategoryInviteStatus status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "responded_at")
    private LocalDateTime respondedAt;

    public CategoryInvite(Long categoryId,  Long inviterUserId, Long invitedUserId) {
        this.categoryId = categoryId;
        this.inviterUserId = inviterUserId;
        this.invitedUserId = invitedUserId;
        this.status = CategoryInviteStatus.PENDING;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.respondedAt = null;
    }

    public void setStatus (CategoryInviteStatus status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
        this.respondedAt = LocalDateTime.now();
    }
}
