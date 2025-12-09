package com.soi.backend.domain.media.controller;

import com.soi.backend.domain.media.entity.FileType;
import com.soi.backend.domain.media.entity.UsageType;
import com.soi.backend.domain.media.service.MediaService;
import com.soi.backend.global.ApiResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/media")

@Tag(name = "사진, 비디오, 음성 파일을 전송하는 API")
public class MediaController {

    private final MediaService mediaService;

    @Operation(summary = "미디어 업로드", description = "단일, 여러개의 파일을 올릴 수 있습니다. 여러개의 파일 업로드시 , 로 구분해서 type을 명시합니다." +
            "id값은 고유 id를 받습니다.")
    @PostMapping(path = "/upload", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<ApiResponseDto<List<String>>> uploadMedia(
            @RequestParam("types") List<String> types,
            @RequestParam("usageTypes") List<String> useageTypes,
            @RequestParam("userId") Long userId,
            @RequestParam("refId") Long refId,
            @RequestParam("usageCount") Long usageCount,
            @RequestPart("files") List<MultipartFile> files) throws IOException {
        List<String> urls = mediaService.uploadMedia(types,useageTypes,userId,refId,files,usageCount);
        return ResponseEntity.ok(ApiResponseDto.success(urls, "파일 저장성공"));
    }

    @Operation(summary = "Presigned URL 요청", description = "DB에 저장된 S3 key를 입력하면 1시간 유효한 접근 URL을 반환합니다.")
    @GetMapping("/get-url")
    public ResponseEntity<ApiResponseDto<List<String>>> getPresignedUrl(@RequestParam List<String> key) {
        List<String> url = mediaService.getPresignedUrlByKey(key);
        return ResponseEntity.ok(ApiResponseDto.success(url, "Presigned URL 생성 성공"));
    }
}
