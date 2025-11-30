package com.soi.backend.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor

public class UserCreateReqDto {
    private String name;
    private String userId;
    private String phoneNum;
    private String birthDate;
    private String profileImage;
    private Boolean serviceAgreed;
    private Boolean privacyPolicyAgreed;
    private Boolean marketingAgreed;
}