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

    @Column(name = "category_id")
    private Long categoryId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "custom_name")
    private String customName;

    @Column(name = "is_pinned")
    private Boolean isPinned;

    @Column(name = "is_read")
    private Boolean isRead;

    @Column(name = "last_viewed_at")
    private LocalDateTime lastViewedAt;

    public CategoryUser(Long categoryId, Long inviterUserId) {
        this.categoryId = categoryId;
        this.userId = inviterUserId;
        this.customName = "";
        this.isPinned = false;
        this.isRead = false;
        this.lastViewedAt = null;
    }
}
