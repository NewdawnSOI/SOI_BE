package com.soi.backend.domain.category.service;

import com.soi.backend.domain.category.entity.Category;
import com.soi.backend.domain.category.entity.CategoryUser;
import com.soi.backend.domain.category.repository.CategoryRepository;
import com.soi.backend.domain.category.repository.CategoryUserRepository;
import com.soi.backend.domain.media.service.MediaService;
import com.soi.backend.domain.post.entity.Post;
import com.soi.backend.domain.post.entity.PostStatus;
import com.soi.backend.domain.post.entity.PostType;
import com.soi.backend.domain.post.repository.PostRepository;
import com.soi.backend.domain.post.service.PostService;
import com.soi.backend.global.exception.CustomException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor

public class CategorySetService {
    private final CategoryRepository categoryRepository;
    private final CategoryUserRepository categoryUserRepository;
    private final PostRepository postRepository;
    private final MediaService mediaService;

    @Transactional
    public void setLastViewed(Long categoryId, Long userId) {
        CategoryUser categoryUser = categoryUserRepository.findByCategoryIdAndUserId(categoryId, userId)
                .orElseThrow(() -> new CustomException(categoryId + "번 카테고리에 " + userId + " 유저가 속해있지 않음",  HttpStatus.NOT_FOUND));

        categoryUser.setLastViewedAt();
        categoryUserRepository.save(categoryUser);
    }

    @Transactional
    public Boolean setPinned(Long categoryId, Long userId) {
        CategoryUser categoryUser = categoryUserRepository.findByCategoryIdAndUserId(categoryId, userId)
                .orElseThrow(() -> new CustomException(categoryId + "번 카테고리에 " + userId + " 유저가 속해있지 않음",  HttpStatus.NOT_FOUND));

        categoryUser.setIsPinned();
        categoryUserRepository.save(categoryUser);
        return categoryUser.getIsPinned();
    }

    @Transactional
    public void setLastUploadedAndProfile(Long categoryId, Long userId, String postFileKey) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CustomException("카테고리를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        if (category.getCategoryProfileKey().isEmpty()) {
            category.setLastPhotoUploadedByAndProfile(userId, postFileKey);
        } else {
            category.setLastPhotoUploadedAt(userId);
        }
        categoryRepository.save(category);
    }

    @Transactional
    public Boolean setName(Long categoryId, Long userId, String name) {
        CategoryUser categoryUser = categoryUserRepository.findByCategoryIdAndUserId(categoryId, userId)
                .orElseThrow(() -> new CustomException(categoryId + "번 카테고리에 " + userId + " 유저가 속해있지 않음",  HttpStatus.NOT_FOUND));

        if (name == null || name.isEmpty()) {
            categoryUser.setCustomName("");
        } else {
            categoryUser.setCustomName(name);
        }
        categoryUserRepository.save(categoryUser);

        return true;
    }

    @Transactional
    public Boolean setProfile(Long categoryId, Long userId, String profileImageKey) {
        CategoryUser categoryUser = categoryUserRepository.findByCategoryIdAndUserId(categoryId, userId)
                .orElseThrow(() -> new CustomException(categoryId + "번 카테고리에 " + userId + " 유저가 속해있지 않음",  HttpStatus.NOT_FOUND));

        if (!categoryUser.getCustomProfile().isEmpty()) {
            mediaService.removeMedia(categoryUser.getCustomProfile());
        }

        if (profileImageKey == null || profileImageKey.isEmpty()) {
            categoryUser.setCustomProfile("");
        } else {
            categoryUser.setCustomProfile(profileImageKey);
            categoryUserRepository.save(categoryUser);
        }

        return true;
    }

    @Transactional
    public void setCategoryProfile(Post post) {
        Category category = categoryRepository.findById(post.getCategoryId())
                .orElseThrow(() -> new CustomException(post.getCategoryId() + "를 찾을 수 없습니다.",  HttpStatus.NOT_FOUND));

        // 카테고리 대표이미지 변경
        if (category.getCategoryProfileKey().equals(post.getFileKey()) && !post.getFileKey().isBlank()) {
            Optional<Post> topPost = postRepository.findTopByCategoryIdAndStatusOrderByCreatedAtDesc(category.getId(), PostStatus.ACTIVE);
            if (topPost.isPresent()) {
                category.setProfileKey(topPost.get().getFileKey());
            } else {
                category.setProfileKey("");
            }
        }

        // 커스텀 카테고리에 있는것도 삭제
//        if (post.getFileKey() == null || post.getFileKey().isBlank()) return;
        if (post.getPostType() != PostType.TEXT) {
            categoryUserRepository.clearCustomProfileByCategoryIdAndFileKey(category.getId(), post.getFileKey());
        }
    }

    @Transactional
    public Boolean setIsAlert(Long categoryUserId, Long userId) {
        CategoryUser categoryUser = categoryUserRepository.findByUserIdAndCategoryId(userId, categoryUserId)
                .orElseThrow();

        categoryUser.setIsAlert();
        categoryUserRepository.save(categoryUser);
        return true;
    }

}
