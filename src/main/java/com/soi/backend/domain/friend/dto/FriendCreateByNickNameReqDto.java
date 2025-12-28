package com.soi.backend.domain.friend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@AllArgsConstructor

public class FriendCreateByNickNameReqDto {
    private Long requesterId;
    private String receiverNickName;
}
