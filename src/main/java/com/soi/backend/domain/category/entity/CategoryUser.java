package com.soi.backend.domain.category.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor

@Table(name = "categories_user", schema = "soi")
public class CategoryUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "category_id", nullable = false)
    private Long categoryId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "custom_name", nullable = false)
    private String customName;

    @Column(name = "custom_profile", nullable = false)
    private String customProfile;

    @Column(name = "is_pinned")
    private Boolean isPinned;

    @Column(name = "is_read")
    private Boolean isRead;

    @Column(name = "last_viewed_at")
    private LocalDateTime lastViewedAt;

    @Column(name = "pinned_at")
    private LocalDateTime pinnedAt;

    public CategoryUser(Long categoryId, Long inviterUserId) {
        this.categoryId = categoryId;
        this.userId = inviterUserId;
        this.customName = "";
        this.customProfile = "";
        this.isPinned = false;
        this.isRead = false;
        this.lastViewedAt = null;
    }

    public CategoryUser(Long categoryId, Long inviterUserId, LocalDateTime lastViewedAt) {
        this.categoryId = categoryId;
        this.userId = inviterUserId;
        this.customName = "";
        this.customProfile = "";
        this.isPinned = false;
        this.isRead = false;
        this.lastViewedAt = lastViewedAt;
    }

    public void setLastViewedAt() {
        this.lastViewedAt = LocalDateTime.now();
    }

    public void setIsPinned() {
        isPinned = !isPinned;
        this.pinnedAt = LocalDateTime.now();
    }
}
