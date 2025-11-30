package com.soi.backend.domain.post.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter

@Table(name = "post", schema = "soi")
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "file_url", nullable = false)
    private String fileUrl;

    @Column(name = "audio_url", nullable = false)
    private String audioUrl;

    @Column(name = "category_id", nullable = false)
    private Long categoryId;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "waveform_data", nullable = false)
    private String waveformData;

    @Column(name = "duration")
    private Integer duration;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private PostStatus status;

    public Post (Long userId, String content, String fileUrl, String audioUrl, Long categoryId, String waveformData, int duration) {
        this.userId = userId;
        this.content = content;
        this.fileUrl = fileUrl;
        this.audioUrl = audioUrl;
        this.categoryId = categoryId;
        this.waveformData = waveformData;
        this.duration = duration;
        this.status = PostStatus.ACTIVE;
        this.isActive = true;
        this.createdAt = LocalDateTime.now();
    }

    public void update(String content, String fileUrl, String audioUrl, String waveformData, int duration) {
        this.content = getContent();
        this.fileUrl = getFileUrl();
        this.audioUrl = getAudioUrl();
        this.waveformData = getWaveformData();
        this.duration = getDuration();
    }

    public void setStatus(PostStatus status, Boolean isActive) {
        if (status == PostStatus.ACTIVE) {
            this.isActive = isActive;
        } else{
            this.status = PostStatus.DELETED;
            this.isActive = isActive;
        }
    }
}
