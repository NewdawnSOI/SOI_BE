package com.soi.backend.domain.user.dto;

import lombok.Getter;

@Getter

public class AuthCheckReqDto {
    String phoneNum;
    String code;
}
