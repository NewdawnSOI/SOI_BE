package com.soi.backend.domain.comment.dto;

import com.soi.backend.domain.comment.entity.CommentType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor

public class CommentRespDto {
    private Long id;
    private String userProfileUrl;
    private String userProfileKey;
    private Long userId;
    private String nickname;
    private String text;
    private Long emojiId;
    private String replyUserName;
    private String audioUrl;
    private String waveFormData;
    private Integer duration;
    private Double locationX;
    private Double locationY;
    private CommentType commentType;
    private String fileUrl;
    private String fileKey;
    private LocalDateTime createdAt;
// 대댓글 위한 정보
    private Long replyCommentCount;
}
