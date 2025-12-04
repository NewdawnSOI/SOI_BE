package com.soi.backend.domain.comment.dto;

import com.soi.backend.domain.comment.entity.CommentType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter

public class CommentRespDto {
    private String userProfile;
    private String nickname;
    private String text;
    private Long emojiId;
    private String audioUrl;
    private String waveFormData;
    private Integer duration;
    private Double locationX;
    private Double locationY;
    private CommentType commentType;
}
