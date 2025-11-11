package com.soi.backend.domain.category.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class CategoryCreateReqDto {

    private Long requesterId; // 카테고리를 만든 유저 id > 대표사진이 포함되어 있으면, 해당 유저 id값 사용
    private String name; // 카테고리 이름
    private String categoryPhotoKey; // 카테고리 대표 사진, 없으면 ""
    private List<Long> receiverIds; // 카테고리에 포함되는 유저 id 값들
    private Boolean isPublic; // 공개, 비공개 설정
}
