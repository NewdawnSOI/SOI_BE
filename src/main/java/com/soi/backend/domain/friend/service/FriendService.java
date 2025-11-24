package com.soi.backend.domain.friend.service;

import com.soi.backend.domain.friend.dto.FriendReqDto;
import com.soi.backend.domain.friend.dto.FriendRespDto;
import com.soi.backend.domain.friend.dto.FriendUpdateRespDto;
import com.soi.backend.domain.friend.entity.Friend;
import com.soi.backend.domain.friend.entity.FriendStatus;
import com.soi.backend.domain.friend.repository.FriendRepository;
import com.soi.backend.global.exception.CustomException;
import com.soi.backend.domain.notification.entity.NotificationType;
import com.soi.backend.domain.notification.repository.NotificationRepository;
import com.soi.backend.domain.notification.service.NotificationService;
import com.soi.backend.domain.user.dto.UserFindRespDto;
import com.soi.backend.domain.user.entity.User;
import com.soi.backend.domain.user.repository.UserRepository;
import com.soi.backend.domain.user.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j

public class FriendService {
    private final FriendRepository friendRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final NotificationService notificationService;
    private final NotificationRepository notificationRepository;

    @Transactional
    public FriendRespDto createFriendRequest(FriendReqDto friendReqDto) {
        if (!userService.checkUserExists(friendReqDto)) {
            throw new CustomException("유저 정보를 찾을 수 없습니다.");
        }

        Long receiverId = null;
        Long requesterId = null;
        Optional<Friend> existing = friendRepository
                .findByRequesterIdAndReceiverId(friendReqDto.getRequesterId(), friendReqDto.getReceiverId());

        Friend friend;

        if (existing.isPresent()) {
            friend = existing.get();
            if (friend.getStatus().equals(FriendStatus.BLOCKED)) {
                throw new CustomException("차단된 친구 관계입니다.", HttpStatus.FORBIDDEN);
            }
            if (!friend.getRequesterDeleted() && !friend.getReceiverDeleted()) {
                throw new CustomException("이미 친구관계가 존재합니다.", HttpStatus.CONFLICT);
            }

            // 친구 관계에서 requester가 친구를 삭제하고, 다시 친구 요청을 보냈을때
            // 친구 수락 알림이 requester -> receiver가 되어야함
            if (friend.getRequesterDeleted()) {
                requesterId = friend.getRequesterId();
                receiverId = friend.getReceiverId();
            }
            // 친구 관계에서 receiver가 친구를 삭제하고, 다시 친구 요청을 보냈을때
            // 친구 수락 알림이 receiver -> requester가 되어야함
            else if (friend.getReceiverDeleted()) {
                requesterId = friend.getReceiverId();
                receiverId = friend.getRequesterId();
            }
            friendRepository.save(friend);
        } else {
            friend = new Friend(friendReqDto.getRequesterId(), friendReqDto.getReceiverId(), FriendStatus.PENDING);
            requesterId = friend.getRequesterId();
            receiverId = friend.getReceiverId();
            friendRepository.save(friend);
        }

        // 알림 생성
        Long notificationId = notificationService.sendFriendRequestNotification(
                requesterId,
                receiverId,
                friend.getId(),
                notificationService.makeMessage(requesterId,"", NotificationType.FRIEND_REQUEST
                )
        );

        return toDto(friend, notificationId);
    }

    @Transactional
    public FriendRespDto updateFriendRequest(FriendUpdateRespDto friendUpdateRespDto) {
        Friend friend = friendRepository.findById(friendUpdateRespDto.getId())
                .orElseThrow(() -> new CustomException("유저 정보를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
        Long receiverId = null;
        Long requesterId = null;

        // 요청이
        switch (friendUpdateRespDto.getStatus()) {
            case BLOCKED:
                friend.SetFriendStatus(friendUpdateRespDto.getStatus());
                Friend savedFriend = friendRepository.save(friend);
                return toDto(savedFriend, null);
            case ACCEPTED:
                // 친구 관계에서 requester가 친구를 삭제하고, 다시 친구 요청을 보낸게 수락이 되었을때
                // 친구 수락 알림이 receiver -> requester가 되어야함
                if (friend.getRequesterDeleted()) {
                    friend.SetRequesterDeleted(false);
                    requesterId = friend.getReceiverId();
                    receiverId = friend.getRequesterId();
                }
                // 친구 관계에서 receiver가 친구를 삭제하고, 다시 친구 요청을 보낸게 수락이 되었을때
                // 친구 수락 알림이 requester -> receiver가 되어야함
                else if (friend.getReceiverDeleted()) {
                    friend.SetReceiverDeleted(false);
                    requesterId = friend.getRequesterId();
                    receiverId = friend.getReceiverId();
                }
                // 최초 친구 관계 요청이 수락이 되었을 때
                // 친구 수락 알림이 receiver -> requester가 되어야함
                else {
                    requesterId = friend.getReceiverId();
                    receiverId = friend.getRequesterId();
                }
                friend.SetFriendStatus(friendUpdateRespDto.getStatus());
                break;
            case CANCELLED:
                friendRepository.deleteById(friend.getId());
                notificationRepository.deleteById(notificationRepository.findByFriendId(friend.getId()).get().getFriendId());
                return new FriendRespDto(friend.getId(), friend.getRequesterId(),
                    friend.getReceiverId(), null, friendUpdateRespDto.getStatus(), LocalDateTime.now());
        }

        Friend savedFriend = friendRepository.save(friend);

        // 알림 생성
        Long notificationId = notificationService.sendFriendRequestNotification(
                requesterId,
                receiverId,
                savedFriend.getId(),
                notificationService.makeMessage(requesterId, "", NotificationType.FRIEND_RESPOND
                )
        );

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
            if (friend.get().getStatus().equals(FriendStatus.PENDING)) {
                throw new CustomException("아직 친구 관계가 맺어지지 않았습니다.", HttpStatus.FORBIDDEN);
            }
            Friend friendToDelete = friend.get();

            // 만약 삭제를 요청한 사람이 Friend 관계에서 Requester라면 -> RequesterDeleted를 삭제해야함
            if (friendToDelete.getRequesterId().equals(friendReqDto.getRequesterId())) {
                friendToDelete.SetRequesterDeleted(true);
            }
            // 만약 삭제를 요청한 사람이 Friend 관계에서 Receiver라면 -> ReceiverDeleted를 삭제해야함
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

    // 친구 차단 기능
    @Transactional
    public Boolean blockFriend(FriendReqDto friendReqDto) {
        if (!userService.checkUserExists(friendReqDto)) {
            throw new CustomException("유저 정보를 찾을 수 없습니다.");
        }

        Optional<Friend> existing = friendRepository
                .findByRequesterIdAndReceiverId(friendReqDto.getRequesterId(), friendReqDto.getReceiverId());

        Friend friend;

        if (existing.isPresent()) {
            friend = existing.get();

            if (friend.getStatus().equals(FriendStatus.BLOCKED)) {
                throw new CustomException("이미 치단된 유저입니다.", HttpStatus.CONFLICT);
            }
            friend.SetFriendStatus(FriendStatus.BLOCKED);
            friendRepository.save(friend);
        } else {
            friend = new Friend(friendReqDto.getRequesterId(), friendReqDto.getReceiverId(), FriendStatus.BLOCKED);
            friendRepository.save(friend);
        }
        return true;
    }

    // 친구 차단 해제 기능
    // 차단을 한 사람만 차단을 풀 수 있도록 설정
    // 즉, 차단당시 requester == 차단 풀릴때 requester
    @Transactional
    public Boolean unBlockFriend(FriendReqDto friendReqDto) {
        if (!userService.checkUserExists(friendReqDto)) {
            throw new CustomException("유저 정보를 찾을 수 없습니다.");
        }

        Optional<Friend> existing = friendRepository
                .findFriendByRequesterIdAndReceiverId(friendReqDto.getRequesterId(), friendReqDto.getReceiverId());

        Friend friend;

        if (existing.isPresent()) {
            friend = existing.get();
            friendRepository.delete(friend);
        } else {
            throw new CustomException("차단된 친구관계를 찾을 수 없습니다.");
        }
        return true;
    }

    private FriendRespDto toDto(Friend friend, Long notificationId) {
        return new FriendRespDto(friend.getId(), friend.getRequesterId(),
                friend.getReceiverId(), notificationId, friend.getStatus(), LocalDateTime.now());
    }

    public Boolean isAllFriend(Long requesterId, List<Long> receiverIds) {
        for (Long receiverId : receiverIds) {
            if (!friendRepository.isFriend(requesterId, receiverId)) {
                return false;
            }
        }

        if (receiverIds.size() < 2) {
            return true;
        }

        for (int i=0; i<receiverIds.size(); i++) {
            for (int j=i+1; j<receiverIds.size(); j++) {
                if (!friendRepository.isFriend(receiverIds.get(i), receiverIds.get(j))) {
                    return false;
                }
            }
        }
        return true;
    }

}
