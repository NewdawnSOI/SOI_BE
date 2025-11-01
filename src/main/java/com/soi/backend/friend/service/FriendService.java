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
import com.soi.backend.user.dto.UserFindRespDto;
import com.soi.backend.user.entity.User;
import com.soi.backend.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

    // 친구 리스트 조회 기능
    public List<UserFindRespDto> getAllFriends(Long userId) {
        List<Friend> friends = friendRepository.findAllAcceptedFriends(userId);

        List<Long> friendIds = friends.stream()
                .map(friend ->
                        friend.getRequesterId().equals(userId)
                        ? friend.getReceiverId() : friend.getRequesterId()
                )
                .distinct()
                .collect(Collectors.toList());

        List<User> users = userRepository.findAllById(friendIds);

        return users.stream()
                .map(user -> new UserFindRespDto(
                        user.getId(),
                        user.getName(),
                        user.getUserId(),
                        user.getProfileImage(),
                        user.isActive()
                ))
                .collect(Collectors.toList());
    }

    // 친구 삭제 기능
    // friendReqDto에서 삭제 요청을 한 사람이 RequesterId에 들어가고, 삭제를 당하는 사람이 Receiver에 들어감
    public Boolean deleteFriend(FriendReqDto friendReqDto) {
        Optional<Friend> friend = friendRepository.findAcceptedFriend(friendReqDto.getRequesterId(), friendReqDto.getReceiverId());

        if(friend.isPresent()) {
            Friend friendToDelete = friend.get();

            // 만약 삭제를 요청한 사람이 Friend 관계에서 Requester라면 -> RequesterDeleted를 삭제해야함
            if (friendToDelete.getRequesterId().equals(friendReqDto.getRequesterId())) {
                friendToDelete.SetRequesterDeleted(true);
            }
            // 그리고 만약 삭제를 요청한 사람이 Friend 관계에서 Receiver라면 -> ReceiverDeleted를 삭제해야함
            else if(friendToDelete.getReceiverId().equals(friendReqDto.getRequesterId())) {
                friendToDelete.SetReceiverDeleted(true);
            }

            // 만약 쌍방 삭제가 이루어졌으면 그냥 컬럼 삭제시키고, 그게 아니면 업데이트된 관계 저장해야함
            if (friendToDelete.getRequesterDeleted() && friendToDelete.getReceiverDeleted()) {
                friendRepository.deleteById(friendToDelete.getId());
            } else {
                friendRepository.save(friendToDelete);
            }
            return true;
        } else {
            throw new CustomException("친구관계를 찾을 수 없음", HttpStatus.NOT_FOUND);
        }
    }
    private FriendRespDto toDto(Friend friend, Long notificationId) {
        return new FriendRespDto(friend.getId(), friend.getRequesterId(),
                friend.getReceiverId(), notificationId, friend.getStatus(), LocalDateTime.now());
    }

}
