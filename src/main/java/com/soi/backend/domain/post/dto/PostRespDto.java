package com.soi.backend.domain.post.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor

public class PostRespDto {
    private Long id;
    private String nickname;
    private String content;
    private String userProfileImageKey;
    private String postFileUrl;
    private String audioFileUrl;
    private String waveformData;
    private int duration;
    private Boolean is_active;
    private LocalDateTime createdAt;
}
