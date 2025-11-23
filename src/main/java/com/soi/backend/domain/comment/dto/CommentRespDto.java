package com.soi.backend.domain.comment.dto;

import com.soi.backend.domain.comment.entity.CommentType;
import com.soi.backend.domain.notification.entity.NotificationType;
import lombok.AllArgsConstructor;

@AllArgsConstructor

public class CommentRespDto {
    private String userProfile;
    private String text;
    private Long emojiId;
    private String audioUrl;
    private String waveformdata;
    private Integer duration;
    private Double locationX;
    private Double locationY;
    private CommentType commentType;
}
