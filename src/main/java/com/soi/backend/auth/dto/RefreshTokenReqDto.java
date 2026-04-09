package com.soi.backend.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class RefreshTokenReqDto {

    @NotBlank
    private String refreshToken;
}
