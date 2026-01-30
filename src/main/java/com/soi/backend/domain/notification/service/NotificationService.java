package com.soi.backend.domain.notification.service;

import com.soi.backend.domain.category.entity.Category;
import com.soi.backend.domain.category.entity.CategoryInvite;
import com.soi.backend.domain.category.entity.CategoryInviteStatus;
import com.soi.backend.domain.category.repository.CategoryInviteRepository;
import com.soi.backend.domain.category.repository.CategoryRepository;
import com.soi.backend.domain.media.service.MediaService;
import com.soi.backend.domain.notification.dto.NotificationGetAllRespDto;
import com.soi.backend.domain.notification.dto.NotificationReqDto;
import com.soi.backend.domain.notification.dto.NotificationRespDto;
import com.soi.backend.domain.notification.entity.Notification;
import com.soi.backend.domain.notification.entity.NotificationType;
import com.soi.backend.domain.notification.repository.NotificationRepository;
import com.soi.backend.domain.user.dto.NotificationUserRespDto;
import com.soi.backend.domain.user.entity.User;
import com.soi.backend.domain.user.repository.UserRepository;
import com.soi.backend.global.exception.CustomException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j

public class NotificationService {

    private final MediaService mediaService;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final CategoryInviteRepository categoryInviteRepository;

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
            Long userId, NotificationType filterType, boolean isInclude, int page) {

        Pageable pageable = PageRequest.of(page,10);
        List<NotificationRespDto> notificationRespDtos = new ArrayList<>();
        List<Notification> notifications = notificationRepository.getAllByReceiverIdOrderByCreatedAtDesc(userId, pageable);

        // requesterId들 뽑기
        Set<Long> requesterIds = notifications.stream()
                .map(Notification::getRequesterId)
                .collect(Collectors.toSet());

        // Category_invite의 notification categoryId 수집
        Set<Long> categoryIds = notifications.stream()
                .filter(n -> n.getType() == NotificationType.CATEGORY_INVITE)
                .map(Notification::getCategoryId)
                .collect(Collectors.toSet());

        // 카테고리 Id로 카테고리 초대내역 한번에 가져오고, 해당되는 유저 id 다 가져오기
        List<CategoryInvite> allInvites = categoryInviteRepository.findAllByCategoryIdIn(categoryIds);

        Set<Long> inviteUserIds = allInvites.stream()
                .flatMap(invite ->
                        Stream.of(invite.getInviterUserId(), invite.getInvitedUserId()))
                .collect(Collectors.toSet());

        // User 조회 대상 ID 합치기
        Set<Long> allUserIds = new HashSet<>(requesterIds);
        allUserIds.addAll(inviteUserIds);


        Map<Long, User> userMap = userRepository.findAllById(allUserIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));


        Map<Long, List<CategoryInvite>> inviteMap =
                categoryInviteRepository.findAllByCategoryIdIn(categoryIds).stream()
                        .collect(Collectors.groupingBy(CategoryInvite::getCategoryId));

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

            User requester = userMap.get(notification.getRequesterId());

            List<NotificationUserRespDto> notificationUserRespDtoList = null;

            if (notification.getType().equals(NotificationType.CATEGORY_INVITE)) {
                List<CategoryInvite> invites =
                        inviteMap.getOrDefault(notification.getCategoryId(), List.of());

                Set<Long> relatedUserIds = new HashSet<>();

                for (CategoryInvite categoryInvite : invites) {
                    if (categoryInvite.getStatus() == CategoryInviteStatus.ACCEPTED
                    || categoryInvite.getStatus() == CategoryInviteStatus.PENDING) {

                        if (!categoryInvite.getInvitedUserId().equals(userId)) {
                            relatedUserIds.add(categoryInvite.getInvitedUserId());
                        }
                        if (!categoryInvite.getInviterUserId().equals(userId)) {
                            relatedUserIds.add(categoryInvite.getInviterUserId());
                        }
                    }
                }

                List<User> users = relatedUserIds.stream()
                        .map(userMap::get)
                        .filter(Objects::nonNull)
                        .toList();

                notificationUserRespDtoList = NotificationUserRespDto.toDto(users);
            }

            String imageKey =  notification.getImageKey();
            String profileKey = userRepository.getProfileImageByUserId(notification.getRequesterId());
            Long id = parseId(notification);
            NotificationType type = notification.getType();

            String imageUrl = (imageKey == null || imageKey.isEmpty())
                    ? null
                    : mediaService.getPresignedUrlByKey(imageKey);

            String profileUrl = (profileKey == null || profileKey.isEmpty())
                    ? null
                    : mediaService.getPresignedUrlByKey(profileKey);

            NotificationRespDto notificationRespDto = new NotificationRespDto(
                    notification.getId(),
                    notification.getTitle(),
                    requester.getName(),
                    requester.getNickname(),
                    profileUrl,
                    imageUrl,
                    type,
                    notification.getIsRead(),
                    parseCategoryId(notification),
                    id,
                    notificationUserRespDtoList
            );
            notificationRespDtos.add(notificationRespDto);
        }
        return notificationRespDtos;
    }

    public NotificationGetAllRespDto getAllNotifications(Long userId, int page) {
        return new NotificationGetAllRespDto(
                bindNotificationDtos(userId, NotificationType.FRIEND_REQUEST, false, page));
    }

    public List<NotificationRespDto> getAllFriendNotifications(Long userId, int page) {
        return bindNotificationDtos(userId, NotificationType.FRIEND_REQUEST, true, page);
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
            Long requesterId, Long receiverId, Long postId, Long categoryId, String title, String imageKey) {

        NotificationReqDto dto = NotificationReqDto.builder()
                .requesterId(requesterId)
                .receiverId(receiverId)
                .type(NotificationType.PHOTO_ADDED)
                .title(title)
                .postId(postId)
                .categoryId(categoryId)
                .imageKey(imageKey)
                .build();

        createNotification(dto);
    }

    @Transactional
    public void sendPostCommentNotification(
            Long requesterId, Long receiverId, Long commentId, Long postId, Long categoryId, String title, NotificationType type) {

        NotificationReqDto dto = NotificationReqDto.builder()
                .requesterId(requesterId)
                .receiverId(receiverId)
                .type(type)
                .title(title)
                .postId(postId)
                .categoryId(categoryId)
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

    @Transactional
    public void setIsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new CustomException("알림을 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        notification.setIsRead();
        notificationRepository.save(notification);
    }

    public String makeMessage(Long requesterId, String targetName, NotificationType type ) {
        String requesterName = userRepository.findById(requesterId)
                .orElseThrow(() -> new CustomException("요청 유저 없음", HttpStatus.NOT_FOUND))
                .getName();
        return switch (type) {
            case FRIEND_REQUEST -> requesterName + " 님이 친구추가 요청을 보냈습니다.";
            case FRIEND_RESPOND -> requesterName + " 님이 친구요청을 수락하였습니다.";
            case CATEGORY_INVITE -> requesterName + " 님이 \"" + targetName + "\" 카테고리에 초대하였습니다.";
            case CATEGORY_ADDED -> requesterName + " 님의 \"" + targetName + "\" 카테고리에 추가되었습니다.";
//            case PHOTO_ADDED -> requesterName + " 님이 " + targetName + " 카테고리에 게시물을 추가하였습니다.";
//            case COMMENT_ADDED -> requesterName + " 님이" + targetName + " 게시물에 댓글을 남겼습니다.";
//            case COMMENT_AUDIO_ADDED -> requesterName + " 님이" + targetName + " 게시물에 음성 댓글을 남겼습니다.";
//            case CATEGORY_INVITE -> requesterName + " 님이 카테고리에 초대하였습니다.";
//            case CATEGORY_ADDED -> requesterName + " 님의 카테고리에 추가되었습니다.";
            case PHOTO_ADDED -> requesterName + " 님이 카테고리에 게시물을 추가하였습니다.";
            case COMMENT_ADDED -> requesterName + " 님이 게시물에 댓글을 남겼습니다.";
            case COMMENT_AUDIO_ADDED -> requesterName + " 님이 게시물에 음성 댓글을 남겼습니다.";
            case COMMENT_REACT_ADDED -> requesterName + " 님이 게시물에 반응을 남겼습니다.";
            default -> "";
        };
    }

    private Long parseId(Notification notification) {
        Long id;
        switch (notification.getType()) {
            case FRIEND_REQUEST, FRIEND_RESPOND -> id = notification.getFriendId();
            case CATEGORY_INVITE, CATEGORY_ADDED -> id =notification.getCategoryId();
            case PHOTO_ADDED, COMMENT_AUDIO_ADDED, COMMENT_ADDED, COMMENT_REACT_ADDED -> id = notification.getPostId();
            default -> id = null;
        }
        return id;
    }

    private Long parseCategoryId(Notification notification) {
        Long id;
        switch (notification.getType()) {
            case PHOTO_ADDED, COMMENT_AUDIO_ADDED, COMMENT_ADDED, COMMENT_REACT_ADDED -> id = notification.getCategoryId();
            default -> id = null;
        }
        return id;
    }

//    private List<Long>

}
