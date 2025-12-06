package com.soi.backend.domain.user.dto;


import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class UserRespDto {
    private Long id;
    private String nickname;
    private String name;
    private String profileImageKey;
    private String birthDate;
    private String phoneNum;
}
