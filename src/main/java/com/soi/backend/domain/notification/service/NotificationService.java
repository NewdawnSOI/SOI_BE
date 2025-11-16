package com.soi.backend.domain.notification.service;

import com.soi.backend.domain.notification.dto.NotificationReqDto;
import com.soi.backend.domain.notification.entity.Notification;
import com.soi.backend.domain.notification.entity.NotificationType;
import com.soi.backend.domain.notification.repository.NotificationRepository;
import com.soi.backend.domain.user.repository.UserRepository;
import com.soi.backend.global.exception.CustomException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j

public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Transactional
    public Long createNotification(NotificationReqDto dto) {

        Notification notification = new Notification(
                dto.getRequesterId(),
                dto.getReceiverId(),
                dto.getType(),
                dto.getTitle(),
                dto.getFriendId(),
                dto.getCategoryId(),
                dto.getCategoryInviteId(),
                dto.getCommentId()
        );

        notificationRepository.save(notification);
        return notification.getId();
    }
    @Transactional
    public Long createCategoryNotification(Long requesterId, Long receiverId, NotificationType type,
                                           String title, Long categoryId, Long categoryInvited) {
        NotificationReqDto notificationReqDto = NotificationReqDto.builder()
                .requesterId(requesterId)
                .receiverId(receiverId)
                .type(type)
                .title(title)
                .categoryId(categoryId)
                .categoryInviteId(categoryInvited)
                .build();

        return createNotification(notificationReqDto);
    }

    public Long sendFriendRequestNotification(Long requesterId, Long receiverId, Long friendId, String message) {

        NotificationReqDto dto = NotificationReqDto.builder()
                .requesterId(requesterId)
                .receiverId(receiverId)
                .type(NotificationType.FRIEND_REQUEST)
                .title(message)
                .friendId(friendId)
                .build();

        return createNotification(dto);
    }

    public Long sendCategoryPostNotification(
            Long requesterId, Long receiverId, Long categoryId, String title) {

        NotificationReqDto dto = NotificationReqDto.builder()
                .requesterId(requesterId)
                .receiverId(receiverId)
                .type(NotificationType.PHOTO_ADDED)
                .title(title)
                .categoryId(categoryId)
                .build();

        return createNotification(dto);
    }

    public String makeMessage(Long requesterId, String targetName, NotificationType type ) {
        String requesterName = userRepository.findById(requesterId)
                .orElseThrow(() -> new CustomException("요청 유저 없음", HttpStatus.NOT_FOUND))
                .getUserId();
        return switch (type) {
            case FRIEND_REQUEST -> requesterName + " 님이 친구추가 요청을 보냈습니다.";
            case FRIEND_RESPOND -> requesterName + " 님이 친구요청을 수락하였습니다.";
            case CATEGORY_INVITE -> requesterName + " 님이 " + targetName + " 카테고리에 초대하였습니다.";
            case CATEGORY_ADDED -> requesterName + " 님의 " + targetName + " 카테고리에 추가되었습니다.";
            case PHOTO_ADDED -> requesterName + " 님이 " + targetName + " 카테고리에 게시물을 추가하였습니다.";
            case COMMENT_ADDED -> requesterName + " 님이 댓글을 남겼습니다.";
            default -> "";
        };
    }

}
