package com.soi.backend.domain.category.service;

import com.soi.backend.domain.category.entity.Category;
import com.soi.backend.domain.category.entity.CategoryUser;
import com.soi.backend.domain.category.repository.CategoryRepository;
import com.soi.backend.domain.category.repository.CategoryUserRepository;
import com.soi.backend.domain.media.service.MediaService;
import com.soi.backend.domain.post.service.PostService;
import com.soi.backend.global.exception.CustomException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor

public class CategorySetService {
    private final CategoryRepository categoryRepository;
    private final CategoryUserRepository categoryUserRepository;
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
    public void setLastUploaded(Long categoryId, Long userId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CustomException("카테고리를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        category.setLastPhotoUploadedBy(userId);
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
}
