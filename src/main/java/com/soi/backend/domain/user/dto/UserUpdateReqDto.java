package com.soi.backend.domain.user.dto;

import lombok.Getter;

@Getter

public class UserUpdateReqDto {
    private Long id;
    private String name;
    private String userId;
    private String phoneNum;
    private String birthDate;
    private String profileImageKey;
    private Boolean marketingAgreed;
}
