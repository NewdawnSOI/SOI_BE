package com.soi.backend.domain.friend.dto;

import jakarta.annotation.Nonnull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor

public class FriendReqDto {
    private Long requesterId;
    private Long receiverId;
}
