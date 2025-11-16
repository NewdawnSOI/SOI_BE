package com.soi.backend.domain.media.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.time.LocalDateTime;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor

@Table(name = "media_file", schema = "soi")
public class Media {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "media_key")
    private String mediaKey;

    @Column(name = "uploader_id")
    private Long uploaderId;

    @Enumerated(EnumType.STRING)
    @Column(name = "media_type")
    private FileType mediaType;

    @Enumerated(EnumType.STRING)
    @Column(name = "media_usage_type")
    private UsageType mediaUsageType;

    @Column(name = "usage_ref_id")
    private Long usageRefId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Media (String mediaKey, Long uploaderId, FileType mediaType, UsageType mediaUsageType, Long usageRefId) {
        this.mediaKey = mediaKey;
        this.uploaderId = uploaderId;
        this.mediaType = mediaType;
        this.mediaUsageType = mediaUsageType;
        this.usageRefId = usageRefId;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}
