package com.soi.backend.domain.comment.dto;

import com.soi.backend.domain.comment.entity.CommentType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Getter
@AllArgsConstructor
@NoArgsConstructor

public class CommentReqDto {
    Long userId;
    Long emojiId;
    Long postId;
    String text;
    String audioKey;
    String fileKey;
    String waveformData;
    Integer duration;
    Double locationX;
    Double locationY;
    CommentType commentType;
}
