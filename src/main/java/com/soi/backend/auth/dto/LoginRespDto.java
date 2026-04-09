package com.soi.backend.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor

public class LoginRespDto {
    private String accessToken;
    private String refreshToken;
    private long accessTokenExpiresInMs;
    private long refreshTokenExpiresInMs;
}
