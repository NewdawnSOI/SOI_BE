package com.soi.backend.domain.notification.service;

import com.soi.backend.domain.media.service.MediaService;
import com.soi.backend.domain.notification.dto.NotificationGetAllRespDto;
import com.soi.backend.domain.notification.dto.NotificationReqDto;
import com.soi.backend.domain.notification.dto.NotificationRespDto;
import com.soi.backend.domain.notification.entity.Notification;
import com.soi.backend.domain.notification.entity.NotificationType;
import com.soi.backend.domain.notification.repository.NotificationRepository;
import com.soi.backend.domain.user.entity.User;
import com.soi.backend.domain.user.repository.UserRepository;
import com.soi.backend.global.exception.CustomException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j

public class NotificationService {

    private final MediaService mediaService;
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
                dto.getPostId(),
                dto.getCommentId(),
                dto.getImageKey()
        );

        notificationRepository.save(notification);
        return notification.getId();
    }
    @Transactional
    public Long createCategoryNotification(Long requesterId, Long receiverId, NotificationType type,
                                           String title, Long categoryId, Long categoryInvited, String imageKey) {
        NotificationReqDto notificationReqDto = NotificationReqDto.builder()
                .requesterId(requesterId)
                .receiverId(receiverId)
                .type(type)
                .title(title)
                .categoryId(categoryId)
                .categoryInviteId(categoryInvited)
                .imageKey(imageKey)
                .build();

        return createNotification(notificationReqDto);
    }

    public List<NotificationRespDto> bindNotificationDtos(
            Long userId, NotificationType filterType, boolean isInclude) {
        List<NotificationRespDto> notificationRespDtos = new ArrayList<>();
        List<Notification> notifications = notificationRepository.getAllByReceiverIdOrderByCreatedAt(userId);

        User user =  userRepository.findById(userId)
                .orElseThrow(() -> new CustomException("User not found", HttpStatus.NOT_FOUND));

        for (Notification notification : notifications) {
            if (isInclude) { // isInclude == true 면 해당 타입만 포함해서 가져옴,
                            // isInclude == false 면 해당 타입만 제외하고 가져옴
                if (notification.getType() != filterType) {
                    continue;
                }
            } else {
                if (notification.getType() == filterType) {
                    continue;
                }
            }

            String imageKey =  notification.getImageKey();
            String profileKey = userRepository.getProfileImageByUserId(notification.getRequesterId());
            Long id = parseId(notification);

            String imageUrl = (imageKey == null || imageKey.isEmpty())
                    ? null
                    : mediaService.getPresignedUrlByKey(imageKey);

            String profileUrl = (profileKey == null || profileKey.isEmpty())
                    ? null
                    : mediaService.getPresignedUrlByKey(profileKey);
            NotificationRespDto notificationRespDto = new NotificationRespDto(
                    notification.getTitle(),
                    user.getName(),
                    user.getNickname(),
                    profileUrl,
                    imageUrl,
                    id
            );
            notificationRespDtos.add(notificationRespDto);
        }
        return notificationRespDtos;
    }

    public NotificationGetAllRespDto getAllNotifications(Long userId) {
        return new NotificationGetAllRespDto(
                bindNotificationDtos(userId, NotificationType.FRIEND_REQUEST, false));
    }

    public List<NotificationRespDto> getAllFriendNotifications(Long userId) {
        return bindNotificationDtos(userId, NotificationType.FRIEND_REQUEST, true);
    }

    @Transactional
    public Long sendFriendNotification(Long requesterId, Long receiverId, Long friendId, String message, NotificationType type) {

        NotificationReqDto dto = NotificationReqDto.builder()
                .requesterId(requesterId)
                .receiverId(receiverId)
                .type(type)
                .title(message)
                .friendId(friendId)
                .imageKey("")
                .build();

        return createNotification(dto);
    }

    @Transactional
    public void sendCategoryPostNotification(
            Long requesterId, Long receiverId, Long categoryId, String title, String imageKey) {

        NotificationReqDto dto = NotificationReqDto.builder()
                .requesterId(requesterId)
                .receiverId(receiverId)
                .type(NotificationType.PHOTO_ADDED)
                .title(title)
                .categoryId(categoryId)
                .imageKey(imageKey)
                .build();

        createNotification(dto);
    }

    @Transactional
    public void sendPostCommentNotification(
            Long requesterId, Long receiverId, Long commentId, Long postId, String title) {

        NotificationReqDto dto = NotificationReqDto.builder()
                .requesterId(requesterId)
                .receiverId(receiverId)
                .type(NotificationType.COMMENT_ADDED)
                .title(title)
                .postId(postId)
                .commentId(commentId)
                .imageKey("")
                .build();

        createNotification(dto);
    }

    @Transactional
    public void deleteCategoryNotification(Long userId, Long categoryId) {
        // 유저 아이디를 받아서 receiver id가 유저인 카테고리 알림을 다 삭제함
        List<Long> notificationIds = notificationRepository.findAllIdByCategoryId(userId, categoryId);

        for (Long notificationId : notificationIds) {
            notificationRepository.deleteById(notificationId);
        }
    }
    @Transactional
    public void deleteCategoryInviteNotification(Long userId, Long categoryInviteId) {
        // 유저 아이디를 받아서 receiver id가 유저인 카테고리 초대 알림을 다 삭제함
        List<Long> notificationIds = notificationRepository.deleteCategoryInviteNotification(userId, categoryInviteId);

        for (Long notificationId : notificationIds) {
            notificationRepository.deleteById(notificationId);
        }
    }

    @Transactional
    public void deletePostNotification(Long userId, Long postId) {
        // 유저 아이디를 받아서 receiver id가 유저인 게시물 알림을 다 삭제함
        List<Long> notificationIds = notificationRepository.findAllIdByPostId(userId, postId);

        for (Long notificationId : notificationIds) {
            notificationRepository.deleteById(notificationId);
        }
    }

    @Transactional
    public void deleteCommentNotification(Long userId, Long commentId) {
        // 유저 아이디를 받아서 receiver id가 유저인 댓글 알림을 다 삭제함
        List<Long> notificationIds = notificationRepository.findAllIdByCommentId(userId, commentId);

        for (Long notificationId : notificationIds) {
            notificationRepository.deleteById(notificationId);
        }
    }

    public String makeMessage(Long requesterId, String targetName, NotificationType type ) {
        String requesterName = userRepository.findById(requesterId)
                .orElseThrow(() -> new CustomException("요청 유저 없음", HttpStatus.NOT_FOUND))
                .getName();
        return switch (type) {
            case FRIEND_REQUEST -> requesterName + " 님이 친구추가 요청을 보냈습니다.";
            case FRIEND_RESPOND -> requesterName + " 님이 친구요청을 수락하였습니다.";
            case CATEGORY_INVITE -> requesterName + " 님이 " + targetName + " 카테고리에 초대하였습니다.";
            case CATEGORY_ADDED -> requesterName + " 님의 " + targetName + " 카테고리에 추가되었습니다.";
            case PHOTO_ADDED -> requesterName + " 님이 " + targetName + " 카테고리에 게시물을 추가하였습니다.";
            case COMMENT_ADDED -> requesterName + " 님이" + targetName + " 게시물에 댓글을 남겼습니다.";
            default -> "";
        };
    }

    private Long parseId(Notification notification) {
        Long id;
        switch (notification.getType()) {
            case FRIEND_REQUEST -> id = notification.getFriendId();
            case FRIEND_RESPOND -> id = notification.getFriendId();
            case CATEGORY_INVITE -> id =notification.getCategoryId();
            case CATEGORY_ADDED -> id = notification.getCategoryId();
            case PHOTO_ADDED -> id = notification.getPostId();
            case COMMENT_ADDED -> id = notification.getCommentId();
            default -> id = null;
        }
        return id;
    }

}
