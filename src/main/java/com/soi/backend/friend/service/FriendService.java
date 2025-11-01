package com.soi.backend.friend.service;

import com.soi.backend.friend.dto.FriendReqDto;
import com.soi.backend.friend.dto.FriendRespDto;
import com.soi.backend.friend.dto.FriendUpdateRespDto;
import com.soi.backend.friend.entity.Friend;
import com.soi.backend.friend.entity.FriendStatus;
import com.soi.backend.friend.repository.FriendRepository;
import com.soi.backend.global.exception.CustomException;
import com.soi.backend.notification.entity.NotificationType;
import com.soi.backend.notification.service.NotificationService;
import com.soi.backend.user.entity.User;
import com.soi.backend.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j

public class FriendService {
    private final FriendRepository friendRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Transactional
    public FriendRespDto createFriendRequest(FriendReqDto friendReqDto) {
        if (userRepository.findById(friendReqDto.getRequesterId()).isEmpty()) {
                throw new CustomException("요청 유저를 찾을 수 없습니다.");
        }
        if (userRepository.findById(friendReqDto.getReceiverId()).isEmpty()) {
            throw new CustomException("수신 유저를 찾을 수 없습니다.");
        }

        Optional<Friend> existing = friendRepository
                .findByRequesterIdAndReceiverId(friendReqDto.getRequesterId(), friendReqDto.getReceiverId());
        if (existing.isPresent()) {
            Friend friend = existing.get();
            if (friend.getStatus().equals(FriendStatus.BLOCKED)) {
                throw new CustomException("차단된 친구 관계입니다.", HttpStatus.FORBIDDEN);
            }
            throw new CustomException("이미 친구관계가 존재합니다.", HttpStatus.CONFLICT);
        }

        Friend friend = new Friend(friendReqDto.getRequesterId(), friendReqDto.getReceiverId(), FriendStatus.PENDING);
        Friend savedFriend = friendRepository.save(friend);

        // 알림 생성
        Long notificationId = notificationService.createNofication(
                savedFriend.getId(),
                savedFriend.getRequesterId(),
                friendReqDto.getReceiverId(),
                NotificationType.FRIEND_REQUEST, "친구추가 요청을 보냈습니다.");

        return toDto(savedFriend, notificationId);
    }

    @Transactional
    public FriendRespDto updateFriendRequest(FriendUpdateRespDto friendUpdateRespDto) {
        Optional<Friend> optionalFriend = friendRepository.findById(friendUpdateRespDto.getId());
        if (optionalFriend.isEmpty()) {
            throw new CustomException("삭제되거나 없는 친구 관계입니다.", HttpStatus.FORBIDDEN);
        }

        // 친구 정보 업데이트
        Friend friend = optionalFriend.get();

        // 만약 요청이 취소거나 차단이면 기존에 있던 친구 관계를 삭제해야함
        if (friendUpdateRespDto.getStatus().equals(FriendStatus.CANCELLED)
        ||  friendUpdateRespDto.getStatus().equals(FriendStatus.BLOCKED)) {
            friendRepository.deleteById(friend.getId());
            return new FriendRespDto(friend.getId(), friend.getRequesterId(),
                    friend.getReceiverId(), null, friendUpdateRespDto.getStatus(), LocalDateTime.now());
        } else {
            friend.SetFriendStatus(friendUpdateRespDto.getStatus());
        }

        Friend savedFriend = friendRepository.save(friend);

        // 알림 생성
        Long notificationId = notificationService.createNofication(
                savedFriend.getId(),
                savedFriend.getRequesterId(),
                savedFriend.getReceiverId(),
                NotificationType.FRIEND_REQUEST,
                "친구 수락하였습니다.");

        return toDto(savedFriend, notificationId);
    }

    private FriendRespDto toDto(Friend friend, Long notificationId) {
        return new FriendRespDto(friend.getId(), friend.getRequesterId(),
                friend.getReceiverId(), notificationId, friend.getStatus(), LocalDateTime.now());
    }

}
