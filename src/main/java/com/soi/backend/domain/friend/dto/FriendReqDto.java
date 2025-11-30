package com.soi.backend.domain.friend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter

public class FriendReqDto {
    private Long requesterId;
    private Long receiverId;
}
