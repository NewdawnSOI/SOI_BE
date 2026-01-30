package com.soi.backend.domain.friend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@AllArgsConstructor
@NoArgsConstructor

public class FriendCreateByNickNameReqDto {
    private Long requesterId;
    private String receiverNickName;
}
