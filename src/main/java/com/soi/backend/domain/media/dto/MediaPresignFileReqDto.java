package com.soi.backend.domain.media.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class MediaPresignFileReqDto {

    @NotBlank
    private String originalFileName;

    @NotBlank
    private String fileType;

    @NotBlank
    private String contentType;
}
