package com.soi.backend.domain.friend.dto;

import com.soi.backend.domain.friend.entity.FriendStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor

public class FriendUpdateRespDto {
    private Long id;
    private FriendStatus status;
}
