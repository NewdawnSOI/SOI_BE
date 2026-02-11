package com.soi.backend.domain.post.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor

public class PostRespDto {
    private Long id;
    private String nickname;
    private String content;
    private String userProfileImageKey;
    private String userProfileImageUrl;
    private String postFileKey;
    private String postFileUrl;
    private String audioFileKey;
    private String waveformData;
    private int duration;
    private Boolean is_active;
    private LocalDateTime createdAt;
    private Float savedAspectRatio; // 사진 비율
    private Boolean isFromGallery; // 찍은건지, 사진첩에서 가져온건지
}
