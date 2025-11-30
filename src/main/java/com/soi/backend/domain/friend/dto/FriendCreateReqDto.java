package com.soi.backend.domain.friend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor

public class FriendCreateReqDto {
    private Long requesterId;
    private String receiverPhoneNum;
}
