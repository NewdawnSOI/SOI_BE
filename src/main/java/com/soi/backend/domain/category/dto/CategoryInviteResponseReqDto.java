package com.soi.backend.domain.category.dto;

import com.soi.backend.domain.category.entity.CategoryInviteStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class CategoryInviteResponseReqDto {
    Long categoryId;
    Long responserId;
    CategoryInviteStatus status;
}
