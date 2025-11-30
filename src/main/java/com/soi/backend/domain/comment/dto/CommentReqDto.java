package com.soi.backend.domain.comment.dto;

import com.soi.backend.domain.comment.entity.CommentType;
import lombok.Getter;

@Getter

public class CommentReqDto {
    Long userId;
    Long emojiId;
    Long postId;
    String text;
    String audioUrl;
    String waveformData;
    Integer duration;
    Double locationX;
    Double locationY;
    CommentType commentType;
}
