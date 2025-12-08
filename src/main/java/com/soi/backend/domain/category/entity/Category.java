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

@Table(name = "categories", schema = "soi")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "category_photo_url", nullable = false)
    private String categoryProfileKey;

    @Column(name = "last_photo_uploaded_by", nullable = false)
    private Long lastPhotoUploadedBy;

    @Column(name = "last_photo_uploaded_at")
    private LocalDateTime lastPhotoUploadedAt;

    @Column(name = "is_public")
    private Boolean isPublic;

    public Category(String name, Boolean isPublic) {
        this.name = name;
        this.categoryProfileKey = "";
        this.lastPhotoUploadedBy = null;
        this.isPublic = isPublic;
        this.lastPhotoUploadedAt = LocalDateTime.now();
    }

    public void setLastPhotoUploadedByAndProfile(Long lastPhotoUploadedBy, String categoryProfileKey) {
        this.categoryProfileKey = categoryProfileKey;
        this.lastPhotoUploadedBy = lastPhotoUploadedBy;
        this.lastPhotoUploadedAt = LocalDateTime.now();
    }

    public void setIsPublic(Boolean isPublic) {
        this.isPublic = isPublic;
    }
}
