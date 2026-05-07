package com.soi.backend.domain.media.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;

import java.util.List;

@Getter
public class MediaPresignReqDto {

    @Valid
    @NotEmpty
    private List<MediaPresignFileReqDto> files;
}
