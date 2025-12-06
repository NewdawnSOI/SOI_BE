package com.soi.backend.domain.category.dto;

import com.soi.backend.domain.category.entity.Category;
import com.soi.backend.domain.category.entity.CategoryUser;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter

public class CategoryRespDto {
    private Long id; // 카테고리 id
    private String name; // 카테고리 이름 (커스텀 이름이랑 비교해서 줘야함)
    private List<String> nicknames;
    private String categoryPhotoKey; // 카테고리 프로필 사진 (커스텀 프로필사진이랑 비교해서 줘야함)
    private Boolean isNew; // 카테고리에 새로운 게시물을 읽었는지 여부
    private Integer totalUserNum;

    private Boolean isPinned;
    private List<String> usersProfileKey; // 카테고리에 있는 유저들 프로필 사진
    private LocalDateTime pinnedAt;

    public CategoryRespDto(Category category, CategoryUser categoryUser, List<String> nicknames, List<String> usersProfileKey,
                           String categoryPhotoKey, Integer totalUserNum, LocalDateTime pinnedAt) {
        this.id = category.getId();
        this.name = categoryUser.getCustomName().isEmpty() ? category.getName() : categoryUser.getCustomName();
        this.nicknames = nicknames;
        this.categoryPhotoKey = categoryPhotoKey;
        this.isNew = isNew(category.getLastPhotoUploadedAt(), categoryUser.getLastViewedAt());
        this.isPinned = categoryUser.getIsPinned();
        this.usersProfileKey = usersProfileKey;
        this.pinnedAt = pinnedAt;
        this.totalUserNum = totalUserNum;
    }

    private boolean isNew(LocalDateTime uploadedAt, LocalDateTime viewedAt) {
        if (uploadedAt == null) return false;    // 사진이 없으면 false
        if (viewedAt == null) return true;       // 본 적 없으면 true
        return uploadedAt.isAfter(viewedAt);     // 업로드가 더 최신이면 true
    }
}
