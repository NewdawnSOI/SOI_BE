package com.soi.backend.domain.category.service;

import com.soi.backend.domain.category.dto.CategoryCreateReqDto;
import com.soi.backend.domain.category.dto.CategoryCreateRespDto;
import com.soi.backend.domain.category.entity.Category;
import com.soi.backend.domain.category.entity.CategoryInvite;
import com.soi.backend.domain.category.entity.CategoryUser;
import com.soi.backend.domain.category.repository.CategoryInviteRepository;
import com.soi.backend.domain.category.repository.CategoryRepository;
import com.soi.backend.domain.category.repository.CategoryUserRepository;
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

    @Transactional
    public Long initializeCategory(CategoryCreateReqDto dto) {
        Long categoryId = createCategory(dto);
        createCategoryUser(categoryId, dto.getUsers());
        createCategoryInvite(categoryId, dto.getUserId(), dto.getUsers());
        sendCategoryNotification(categoryId, dto.getUserId(), dto.getUsers());
        return categoryId;
    }

    @Transactional
    public Long createCategory(CategoryCreateReqDto categoryCreateReqDto) {

        String userId = userRepository.findById(categoryCreateReqDto.getUserId())
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

        categoryRepository.save(category);
        return category.getId();
    }

    @Transactional
    public void createCategoryUser(Long categoryId, List<Long> users) {
        List<CategoryUser> categoryUsers = users.stream()
                .map(u -> new CategoryUser(categoryId, u))
                .toList();
        categoryUserRepository.saveAll(categoryUsers);
    }

    @Transactional
    public void createCategoryInvite(Long categoryId, Long inviter, List<Long> users) {
        List<CategoryInvite> categoryInvites = users.stream()
                .map(u -> new CategoryInvite(categoryId, inviter, u))
                .toList();
        categoryInviteRepository.saveAll(categoryInvites);
    }

    @Transactional
    public void sendCategoryNotification(Long categoryId, Long inviterId, List<Long> ids) {
        String userId = userRepository.findById(inviterId).get().getUserId();
        String categoryName = categoryRepository.findById(categoryId).get().getName();
        for (Long id : ids) {
            notificationService.createCategoryNotification(
                    inviterId,
                    id,
                    NotificationType.CATEGORY_INVITE,
                    userId + "님이 " + categoryName + " 카테고리에 초대했습니다.",
                    categoryId,
                    categoryInviteRepository.findByCategoryIdAndInvitedUserId(categoryId, id).get().getCategoryId()
            );
        }
    }
}
