package com.soi.backend.domain.category.dto;

import com.soi.backend.domain.category.entity.CategoryInviteStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class CategoryInviteResponseReqDto {
    Long categoryId;
    Long responserId;
    CategoryInviteStatus status;
}
