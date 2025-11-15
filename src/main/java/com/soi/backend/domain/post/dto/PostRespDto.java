package com.soi.backend.domain.post.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor

public class PostRespDto {
    private String userId;
    private String content;
    private String postFileKey;
    private String audioFileKey;
    private String waveformData;
    private int duration;
    private Boolean is_active;
    private LocalDateTime createdAt;
}
