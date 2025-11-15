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

    @Column(name = "user_id")
    private String userId;

    @Column(name = "content")
    private String content;

    @Column(name = "file_url")
    private String fileUrl;

    @Column(name = "audio_url")
    private String audioUrl;

    @Column(name = "category_id")
    private Long categoryId;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "waveform_data")
    private String waveformData;

    @Column(name = "duration")
    private Integer duration;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private PostStatus status;

    public Post (String userId, String content, String fileUrl, String audioUrl, Long categoryId, String waveformData, int duration) {
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
