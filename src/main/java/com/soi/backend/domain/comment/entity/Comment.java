package com.soi.backend.domain.comment.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor

@Table(name = "comments", schema = "soi")
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "emoji_id")
    private Long emojiId;

    @Column(name = "post_id", nullable = false)
    private Long postId;

    @Column(name = "text", nullable = false)
    private String text;

    @Column(name = "audio_url", nullable = false)
    private String audioUrl;

    @Column(name = "waveform_data", nullable = false)
    private String waveformData;

    @Column(name = "duration", nullable = false)
    private Integer duration;

    @Column(name = "location_x", nullable = false)
    private Double locationX;

    @Column(name = "location_y", nullable = false)
    private Double locationY;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    CommentType commentType;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public Comment(Long userId, Long emojiId, Long postId, String text, String audioUrl,
                    String waveformData, Integer duration, Double locationX, Double locationY, CommentType commentType) {
        this.userId = userId;
        this.emojiId = emojiId;
        this.postId = postId;
        this.text = text;
        this.audioUrl = audioUrl;
        this.waveformData = waveformData;
        this.duration = duration;
        this.locationX = locationX;
        this.locationY = locationY;
        this.commentType = commentType;
        this.createdAt = LocalDateTime.now();
    }
}
