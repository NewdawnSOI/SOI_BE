package com.soi.backend.domain.media.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.util.List;

@Getter
public class MediaRegisterUploadedReqDto {

    @NotEmpty
    private List<String> keys;

    @NotEmpty
    private List<String> usageTypes;

    @NotNull
    private Long refId;
}
