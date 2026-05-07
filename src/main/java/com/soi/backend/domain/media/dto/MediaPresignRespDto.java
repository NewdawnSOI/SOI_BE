package com.soi.backend.domain.media.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MediaPresignRespDto {
    private String key;
    private String uploadUrl;
    private String contentType;
}
