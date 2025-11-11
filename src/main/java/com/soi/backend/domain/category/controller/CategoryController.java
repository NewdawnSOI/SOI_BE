package com.soi.backend.domain.category.controller;

import com.soi.backend.domain.category.dto.CategoryCreateReqDto;
import com.soi.backend.domain.category.service.CategoryService;
import com.soi.backend.global.ApiResponseDto;
import com.soi.backend.global.exception.BaseController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/category")

@Tag(name = "category API", description = "카테고리 관련 API")
public class CategoryController extends BaseController {

    private final CategoryService categoryService;

    @Operation(summary = "카테고리 추가", description = "카테고리를 추가합니다.")
    @PostMapping("/create")
    public ResponseEntity<ApiResponseDto<Long>> create(@RequestBody CategoryCreateReqDto categoryCreateReqDto) {
        try {
            Long categoryId = categoryService.initializeCategory(categoryCreateReqDto);
            return ResponseEntity.ok(ApiResponseDto.success(categoryId,"카테고리 추가 완료"));
        } catch (Exception e) {
            return handleExecption(e);
        }
    }
}
