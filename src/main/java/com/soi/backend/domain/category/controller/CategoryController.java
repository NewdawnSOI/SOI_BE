package com.soi.backend.domain.category.controller;

import com.soi.backend.domain.category.dto.CategoryCreateReqDto;
import com.soi.backend.domain.category.dto.CategoryInviteReqDto;
import com.soi.backend.domain.category.dto.CategoryInviteResponseReqDto;
import com.soi.backend.domain.category.dto.CategoryRespDto;
import com.soi.backend.domain.category.entity.CategoryFilter;
import com.soi.backend.domain.category.service.CategoryService;
import com.soi.backend.global.ApiResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/category")

@Tag(name = "category API", description = "카테고리 관련 API")
public class CategoryController {

    private final CategoryService categoryService;

    @Operation(summary = "카테고리 추가", description = "카테고리를 추가합니다.")
    @PostMapping("/create")
    public ResponseEntity<ApiResponseDto<Long>> create(@RequestBody CategoryCreateReqDto categoryCreateReqDto) {
        Long categoryId = categoryService.initializeCategory(categoryCreateReqDto);
        return ResponseEntity.ok(
                ApiResponseDto.success(categoryId, "카테고리 추가 완료"));
    }

    @Operation(summary = " 카테고리에 유저 추가", description = "이미 생성된 카테고리에 유저를 초대할 때 사용합니다.")
    @PostMapping("/invite")
    public ResponseEntity<ApiResponseDto<Boolean>> inviteUser(@RequestBody CategoryInviteReqDto categoryInviteReqDto) {
        Boolean check = categoryService.inviteUserToCategory(categoryInviteReqDto.getCategoryId(),
                    categoryInviteReqDto.getRequesterId(),
                    categoryInviteReqDto.getReceiverId());
        return ResponseEntity.ok(ApiResponseDto.success(check, "유저 카테고리에 초대 완료"));
    }

    @Operation(summary = "카테고리에 초대된 유저가 초대 승낙여부를 결정하는 API", description = "status에 넣을 수 있는 상태 : PENDING, ACCEPTED, DECLINED, EXPIRED")
    @PostMapping("/invite/response")
    public ResponseEntity<ApiResponseDto<Boolean>> inviteReponse(@RequestBody CategoryInviteResponseReqDto categoryInviteResponseReqDto) {
        Boolean check = categoryService.responseInvite(categoryInviteResponseReqDto);
        return ResponseEntity.ok(ApiResponseDto.success(check, "초대 상태 변경 완료"));
    }

    @Operation(summary = "유저가 속한 카테고리 리스트를 가져오는 API", description = "CategoryFilter : ALL, PUBLIC, PRIVATE -> 옵션에 따라서 전체, 그룹, 개인으로 가져올 수 있음")
    @PostMapping("/find")
    public ResponseEntity<ApiResponseDto<List<CategoryRespDto>>> getCategories(@RequestParam CategoryFilter categoryFilter,
                                                                               @RequestParam Long userId) {
        List<CategoryRespDto> categories = categoryService.findCategories(categoryFilter, userId);
        return ResponseEntity.ok(ApiResponseDto.success(categories, "카테고리 조회 완료"));

    }
}
