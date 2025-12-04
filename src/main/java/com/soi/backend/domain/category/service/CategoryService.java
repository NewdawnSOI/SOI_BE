package com.soi.backend.domain.category.service;

import com.soi.backend.domain.category.dto.CategoryCreateReqDto;
import com.soi.backend.domain.category.dto.CategoryInviteResponseReqDto;
import com.soi.backend.domain.category.dto.CategoryRespDto;
import com.soi.backend.domain.category.entity.*;
import com.soi.backend.domain.category.repository.CategoryInviteRepository;
import com.soi.backend.domain.category.repository.CategoryRepository;
import com.soi.backend.domain.category.repository.CategoryUserRepository;
import com.soi.backend.domain.friend.service.FriendService;
import com.soi.backend.domain.media.service.MediaService;
import com.soi.backend.domain.notification.entity.NotificationType;
import com.soi.backend.domain.notification.service.NotificationService;
import com.soi.backend.domain.post.service.PostService;
import com.soi.backend.domain.user.entity.User;
import com.soi.backend.domain.user.repository.UserRepository;
import com.soi.backend.global.exception.CustomException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor

public class CategoryService {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final CategoryUserRepository categoryUserRepository;
    private final CategoryInviteRepository categoryInviteRepository;
    private final MediaService mediaService;
    private final NotificationService notificationService;
    private final FriendService friendService;
    private final PostService postService;

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

        Category category = new Category(
                    categoryCreateReqDto.getName(),
                    categoryCreateReqDto.getIsPublic()
            );

        // 카테고리 우선 저장하고
        categoryRepository.save(category);

        // 초대유저는 무조건 카테고리-유저 테이블에 생성, 초대 받은 멤버들은 수락하면 생성
        categoryUserRepository.save(new CategoryUser(category.getId(), categoryCreateReqDto.getRequesterId(), LocalDateTime.now()));

        return category.getId();
    }

    @Transactional
    public void createCategoryUser(Long categoryId, Long id) {
        categoryUserRepository.save(new CategoryUser(categoryId, id));
    }

    @Transactional
    public Boolean inviteUserToCategory(Long categoryId, Long requesterId, List<Long> receiverIds) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CustomException("존재하지 않는 카테고리입니다.", HttpStatus.NOT_FOUND));
        userRepository.findById(requesterId)
                .orElseThrow(() -> new CustomException("초대한 유저가 존재하지 않습니다.", HttpStatus.NOT_FOUND));

        List<Long> exists = userRepository.findAllById(receiverIds)
                .stream()
                .map(User::getId)
                .toList();

        if (receiverIds.size() != exists.size()) {
            throw new CustomException("존재하지 않는 유저가 포함되어 있습니다.", HttpStatus.BAD_REQUEST);
        }

        // 요청받은 유저가 이미 category_invite 테이블에 있는지 확인해야함
        List<Long> targetReceiverIds = receiverIds.stream()
                .filter(id -> categoryInviteRepository.findByCategoryIdAndInvitedUserId(categoryId, id).isEmpty())
                .toList();

        // 요청받은 유저가 이미 categoryuser 테이블에 있는지 확인해야함
        List<Long> filterOnlyNewUser = targetReceiverIds.stream()
                .filter(id -> categoryUserRepository.findByUserIdAndCategoryId(id, categoryId).isEmpty())
                .toList();

        if (filterOnlyNewUser.isEmpty()) { // 초대할사람이 아무도 없을때
            throw new CustomException("초대할 유저가 없음 (이미 초대되어있음)",  HttpStatus.BAD_REQUEST);
        }

        // 서로가 다 친구인지 확인
        Boolean allFriends = friendService.isAllFriend(requesterId, filterOnlyNewUser);

        if (allFriends) { // 서로가 다 친구일경우 -> 바로 카테고리에 추가 및 추가됐다는 알림
            filterOnlyNewUser.forEach(id -> {createCategoryUser(categoryId, id);});

//            sendCategoryNotification(categoryId, requesterId, filterOnlyNewUser, NotificationType.CATEGORY_ADDED, category.getCategoryPhotoKey());
            // receiver들에게도 알림
//            receiverIds.forEach(receiverId ->
//                    notificationService.createCategoryNotification(
//                            requesterId,
//                            receiverId,
//                            NotificationType.CATEGORY_ADDED,
//                            notificationService.makeMessage(requesterId, categoryName, NotificationType.CATEGORY_ADDED),
//                            categoryId,
//                            null
//                    )
//            );
            return true;
        }

        createCategoryInvite(categoryId, requesterId, filterOnlyNewUser);
        sendCategoryNotification(categoryId, requesterId, filterOnlyNewUser, NotificationType.CATEGORY_INVITE, category.getCategoryProfileKey());

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
    public void sendCategoryNotification(Long categoryId, Long requesterId, List<Long> receiverIds,  NotificationType type, String imageKey) {
        String userId = userRepository.findById(requesterId).get().getUserId();
        String categoryName = categoryRepository.findById(categoryId).get().getName();
        for (Long receiverId : receiverIds) {
            Long categoryInviteId = categoryInviteRepository.findByCategoryIdAndInvitedUserId(categoryId, receiverId)
                    .orElseThrow(() -> new CustomException("카테고리에 초대되어있지 않습니다.", HttpStatus.NOT_FOUND))
                    .getId();
            notificationService.createCategoryNotification(
                    requesterId,
                    receiverId,
                    type,
                    notificationService.makeMessage(requesterId, categoryName, type),
                    categoryId,
                    categoryInviteId,
                    imageKey
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

    public List<CategoryRespDto> findCategories(CategoryFilter filter, Long userId) {

        List<CategoryRespDto> categories = new ArrayList<>();

        // 1차 : 필터 옵션에 따라서 유저가 속한 모든 카테고리의 id를 가져옴
        List<Long> categoryIds = switch (filter) {
            case ALL -> categoryRepository.findCategoriesByUserIdAndPublicFilter(userId,null);
            case PUBLIC -> categoryRepository.findCategoriesByUserIdAndPublicFilter(userId,true);
            case PRIVATE ->  categoryRepository.findCategoriesByUserIdAndPublicFilter(userId,false);
        };

        // 2차 : 카테고리 아이디랑 유저 아이디로 커스텀 내용 반영해서 Dto 만들기
        for (Long categoryId : categoryIds) {
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new CustomException(categoryId + "번 카테고리를 찾을 수 없음",  HttpStatus.NOT_FOUND));
            CategoryUser categoryUser = categoryUserRepository.findByCategoryIdAndUserId(categoryId, userId)
                    .orElseThrow(() -> new CustomException(categoryId + "번 카테고리에 " + userId + " 유저가 속해있지 않음",  HttpStatus.NOT_FOUND));
            List<User> users = categoryUserRepository.findAllUsersByCategoryIdExceptUser(categoryId, userId);

            categories.add(toDto(category, categoryUser, users));
        }
        sortCategories(categories);
        return categories;
    }

    public CategoryRespDto toDto(Category category, CategoryUser categoryUser, List<User> users) {
        List<String> userProfiles = users.stream()
                .map(user -> {
                    return user.getProfileImageKey();
//                    String image = user.getProfileImageKey();
//                    return image.isEmpty() ? "" : mediaService.getPresignedUrlByKey(image);
                })
                .toList();

        String key = !categoryUser.getCustomProfile().isEmpty()
                ? categoryUser.getCustomProfile()
                : category.getCategoryProfileKey();

        String categoryPhotoKey = key.isEmpty()
                ? ""
                : mediaService.getPresignedUrlByKey(key);

        return new CategoryRespDto(category, categoryUser, userProfiles, categoryPhotoKey, users.size(), categoryUser.getPinnedAt());
    }

    private void sortCategories(List<CategoryRespDto> categories) {

        // 1) pinned=true 그룹
        List<CategoryRespDto> pinned = categories.stream()
                .filter(c -> Boolean.TRUE.equals(c.getIsPinned()))
                .sorted((c1, c2) -> {
                    LocalDateTime t1 = c1.getPinnedAt();
                    LocalDateTime t2 = c2.getPinnedAt();

                    if (t1 == null && t2 == null) return 0;
                    if (t1 == null) return 1;
                    if (t2 == null) return -1;

                    return t2.compareTo(t1); // 최신순
                })
                .toList();

        // 2) pinned=false 그룹 (순서 유지)
        List<CategoryRespDto> notPinned = categories.stream()
                .filter(c -> !Boolean.TRUE.equals(c.getIsPinned()))
                .toList(); // 정렬 X 원래순서

        // 3) 두 그룹을 합쳐 리스트 갱신
        categories.clear();
        categories.addAll(pinned);
        categories.addAll(notPinned);
    }

    @Transactional
    public void deleteCategory(Long userId, Long categoryId) {
        List<CategoryUser> categoryUserIds = categoryUserRepository.findAllByCategoryId(categoryId);
        CategoryUser categoryUserId = categoryUserRepository.findByCategoryIdAndUserId(categoryId, userId)
                .orElseThrow(() -> new CustomException("유저가 카테고리에 속해있지 않습니다.",  HttpStatus.NOT_FOUND));
        Optional<Long> categoryInviteId = categoryInviteRepository.findIdByCategoryIdAndUserId(userId, categoryId);

        if (categoryUserIds.isEmpty()) {
            throw new CustomException("사용자가 게시물에 속해있지 않음", HttpStatus.NOT_FOUND);
        }

        // 카테고리유저 테이블에서 유저 삭제
        categoryUserRepository.deleteById(categoryUserId.getId());
        // 카테고리 관련 알림 삭제
        notificationService.deleteCategoryNotification(userId, categoryId);
        // 카테고리 초대 관련 알림 삭제
        if (categoryInviteId.isPresent()) {
            notificationService.deleteCategoryInviteNotification(userId, categoryInviteId.get());
        }

        if (categoryUserIds.size() == 1) { // 사용자가 카테고리의 마지막 유저인경우
            postService.hardDeletePosts(categoryId);
            categoryRepository.deleteById(categoryId);
        }
    }
}
