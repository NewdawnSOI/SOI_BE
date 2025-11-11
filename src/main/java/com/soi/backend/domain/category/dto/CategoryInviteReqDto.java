package com.soi.backend.domain.category.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class CategoryInviteReqDto {
    private Long requesterId;
    private List<Long> receiverId;
    private Long categoryId;
}
