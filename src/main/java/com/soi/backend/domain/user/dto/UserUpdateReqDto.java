package com.soi.backend.domain.user.dto;

import lombok.Getter;

@Getter

public class UserUpdateReqDto {
    private String name;
    private String nickname;
    private String phoneNum;
    private String birthDate;
    private String profileImageKey;
    private String profileCoverImage;
    private Boolean marketingAgreed;
}
