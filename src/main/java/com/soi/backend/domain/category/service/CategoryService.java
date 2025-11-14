package com.soi.backend.domain.category.service;

import com.soi.backend.domain.category.dto.CategoryCreateReqDto;
import com.soi.backend.domain.category.dto.CategoryInviteResponseReqDto;
import com.soi.backend.domain.category.entity.Category;
import com.soi.backend.domain.category.entity.CategoryInvite;
import com.soi.backend.domain.category.entity.CategoryInviteStatus;
import com.soi.backend.domain.category.entity.CategoryUser;
import com.soi.backend.domain.category.repository.CategoryInviteRepository;
import com.soi.backend.domain.category.repository.CategoryRepository;
import com.soi.backend.domain.category.repository.CategoryUserRepository;
import com.soi.backend.domain.friend.service.FriendService;
import com.soi.backend.domain.media.service.MediaService;
import com.soi.backend.domain.notification.entity.NotificationType;
import com.soi.backend.domain.notification.service.NotificationService;
import com.soi.backend.domain.user.repository.UserRepository;
import com.soi.backend.global.exception.CustomException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor

public class CategoryService {

    private final MediaService mediaService;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final CategoryUserRepository categoryUserRepository;
    private final CategoryInviteRepository categoryInviteRepository;
    private final NotificationService notificationService;
    private final FriendService friendService;

    @Transactional
    public Long initializeCategory(CategoryCreateReqDto dto) {
        Long categoryId = createCategory(dto);
//        createCategoryUser(categoryId, dto.getUsers());
        inviteUserToCategory(categoryId, dto.getRequesterId(), dto.getReceiverIds());
        return categoryId;
    }

    @Transactional
    public Long createCategory(CategoryCreateReqDto categoryCreateReqDto) {

        String userId = userRepository.findById(categoryCreateReqDto.getRequesterId())
                .orElseThrow(() -> new CustomException("카테고리 생성한 유저 id를 찾을 수 없음", HttpStatus.NOT_FOUND))
                .getUserId();

        String imageUrl = null;
        if (categoryCreateReqDto.getCategoryPhotoKey() != null) { // 사진을찍은 상태에서, 새롭게 카테고리를 만들면 이미지 주소 저장
            imageUrl = categoryCreateReqDto.getCategoryPhotoKey();
        }

        System.out.println(imageUrl);
        Category category = new Category(
                    categoryCreateReqDto.getName(),
                    imageUrl,
                    categoryCreateReqDto.getIsPublic()
            );

        if (imageUrl != null) {
            category.setLastPhotoUploadedBy(userId);
        }

        // 카테고리 우선 저장하고
        categoryRepository.save(category);

        // 초대유저는 무조건 카테고리-유저 테이블에 생성, 초대 받은 멤버들은 수락하면 생성
        categoryUserRepository.save(new CategoryUser(category.getId(), categoryCreateReqDto.getRequesterId()));

        return category.getId();
    }

    @Transactional
    public void createCategoryUser(Long categoryId, Long id) {
        categoryUserRepository.save(new CategoryUser(categoryId, id));
    }

    @Transactional
    public Boolean inviteUserToCategory(Long categoryId, Long requesterId, List<Long> receiverIds) {
        categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CustomException("존재하지 않는 카테고리입니다.", HttpStatus.NOT_FOUND));
        userRepository.findById(requesterId)
                .orElseThrow(() -> new CustomException("초대한 유저가 존재하지 않습니다.", HttpStatus.NOT_FOUND));

        List<Long> exists = userRepository.findAllById(receiverIds)
                .stream()
                .map(u -> u.getId())
                .toList();

        if (receiverIds.size() != exists.size()) {
            throw new CustomException("존재하지 않는 유저가 포함되어 있습니다.", HttpStatus.BAD_REQUEST);
        }

        // 서로가 다 친구인지 확인
        Boolean allFriends = friendService.isAllFriend(requesterId, receiverIds);

        if (allFriends) {
            receiverIds.forEach(id -> {createCategoryUser(categoryId, id);});

            String requesterUserId = userRepository.findById(requesterId).get().getUserId();
            String categoryName = categoryRepository.findById(categoryId).get().getName();

            // receiver들에게도 알림
            receiverIds.forEach(receiverId ->
                    notificationService.createCategoryNotification(
                            requesterId,
                            receiverId,
                            NotificationType.CATEGORY_INVITE,
                            requesterUserId + "님의 " + categoryName + " 카테고리에 추가되었습니다.",
                            categoryId,
                            null
                    )
            );

            return true;
        }

        createCategoryInvite(categoryId, requesterId, receiverIds);
        sendCategoryNotification(categoryId, requesterId, receiverIds);

        return true;
    }

    @Transactional
    public Boolean createCategoryInvite(Long categoryId, Long requesterId, List<Long> receiverIds) {
        List<CategoryInvite> categoryInvites = receiverIds.stream()
                .map(u -> new CategoryInvite(categoryId, requesterId, u))
                .toList();
        categoryInviteRepository.saveAll(categoryInvites);
        return true;
    }

    @Transactional
    public void sendCategoryNotification(Long categoryId, Long requesterId, List<Long> receiverIds) {
        String userId = userRepository.findById(requesterId).get().getUserId();
        String categoryName = categoryRepository.findById(categoryId).get().getName();
        for (Long receiverId : receiverIds) {
            Long categoryInviteId = categoryInviteRepository.findByCategoryIdAndInvitedUserId(categoryId, receiverId).get().getId();
            notificationService.createCategoryNotification(
                    requesterId,
                    receiverId,
                    NotificationType.CATEGORY_INVITE,
                    userId + "님이 " + categoryName + " 카테고리에 초대를 보냈습니다.",
                    categoryId,
                    categoryInviteId
            );
        }
    }

    @Transactional
    public Boolean responseInvite(CategoryInviteResponseReqDto inviteResponseDto) {
        categoryRepository.findById(inviteResponseDto.getCategoryId())
                .orElseThrow(() -> new CustomException("존재하지 않는 카테고리입니다.", HttpStatus.NOT_FOUND));

        CategoryInvite categoryInvite = categoryInviteRepository
                .findByCategoryIdAndInvitedUserId(inviteResponseDto.getCategoryId(), inviteResponseDto.getResponserId())
                .orElseThrow(() -> new CustomException("초대된 이력이 없습니다.", HttpStatus.NOT_FOUND));

        switch (inviteResponseDto.getStatus()) {
            case ACCEPTED:
                createCategoryUser(inviteResponseDto.getCategoryId(), inviteResponseDto.getResponserId());
                categoryInvite.setStatus(CategoryInviteStatus.ACCEPTED);
                break;
            case DECLINED:
                categoryInvite.setStatus(CategoryInviteStatus.DECLINED);
                break;
            case EXPIRED:
                categoryInvite.setStatus(CategoryInviteStatus.EXPIRED);
                break;
        }
//        categoryInviteRepository.deleteById(categoryInvite.getId());
        return true;
    }
}
