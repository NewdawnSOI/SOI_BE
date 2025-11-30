package com.soi.backend.domain.post.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter

public class PostCreateReqDto {
    private Long id; // 생성한 유저의 id
    private String userId; // 생성한 유저의 userId
    private String content; // 텍스트 내용
    private String postFileKey; // S3에 업로드된 file Key값
    private String audioFileKey; // S3에 업로드된 audio Key 값
    private List<Long> categoryId; // 등록될 카테고리 id
    private String waveformData; // 음성파일 파형 데이터
    private int duration; // 음성파일 시간
}
