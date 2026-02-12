package com.soi.backend.domain.comment.dto;

import com.soi.backend.domain.comment.entity.CommentType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor

public class CommentRespDto {
    private Long id;
    private String userProfile;
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
    private List<CommentRespDto> children;
}
