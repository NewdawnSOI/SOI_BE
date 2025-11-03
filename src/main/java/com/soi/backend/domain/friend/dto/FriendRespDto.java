package com.soi.backend.domain.friend.dto;

import com.soi.backend.domain.friend.entity.FriendStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class FriendRespDto {
    private Long id; // 친구 추가 요청 id
    private Long requesterId; // 친구 추가 요청한 유저의 id
    private Long receiverId; // 친구 추가 요청을 받은 유저의 id
    private Long notificationId;
    private FriendStatus status;
    private LocalDateTime createdAt;
}
