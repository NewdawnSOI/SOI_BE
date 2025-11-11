package com.soi.backend.domain.category.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class CategoryCreateRespDto {

    private String name;
    private String categoryPhotoUrl;
    private Long lastPhotoUploadedBy;
    private LocalDateTime lastPhotoUploadedAt;
    private Boolean isPublic;
}
