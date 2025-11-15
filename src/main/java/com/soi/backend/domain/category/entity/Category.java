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

    @Column(name = "name")
    private String name;

    @Column(name = "category_photo_url")
    private String categoryPhotoUrl;

    @Column(name = "last_photo_uploaded_by")
    private String lastPhotoUploadedBy;

    @Column(name = "last_photo_uploaded_at")
    private LocalDateTime lastPhotoUploadedAt;

    @Column(name = "is_public")
    private Boolean isPublic;

    public Category(String name, Boolean isPublic) {
        this.name = name;
        this.categoryPhotoUrl = "";
        this.lastPhotoUploadedBy = null;
        this.isPublic = isPublic;
        this.lastPhotoUploadedAt = LocalDateTime.now();
    }

    public void setLastPhotoUploadedBy(String lastPhotoUploadedBy) {
        this.lastPhotoUploadedBy = lastPhotoUploadedBy;
        this.lastPhotoUploadedAt = LocalDateTime.now();
    }

    public void setCategoryPhotoUrl(String categoryPhotoUrl) {
        this.categoryPhotoUrl = categoryPhotoUrl;
    }
}
