package com.soi.backend.domain.friend.dto;

import com.soi.backend.domain.friend.entity.FriendStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FriendCheckRespDto {
    private String phoneNum;
    private Boolean isFriend;
    private FriendStatus status;
}
