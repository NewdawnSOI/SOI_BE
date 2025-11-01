package com.soi.backend.friend.dto;

import com.soi.backend.friend.entity.FriendStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class FriendReqDto {
    private Long requesterId;
    private Long receiverId;
}
