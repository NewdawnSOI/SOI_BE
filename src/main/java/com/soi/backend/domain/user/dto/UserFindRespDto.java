package com.soi.backend.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor

public class UserFindRespDto {
    private Long id;
    private String name;
    private String nickname;
    private String profileImageUrl;
    private boolean isActive;
}
