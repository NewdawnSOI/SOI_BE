package com.soi.backend.domain.user.dto;


import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class UserRespDto {
    private Long id;
    private String userId;
}
