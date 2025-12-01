package com.soi.backend.domain.category.controller;

import com.soi.backend.domain.category.dto.CategoryCreateReqDto;
import com.soi.backend.domain.category.dto.CategoryInviteReqDto;
import com.soi.backend.domain.category.dto.CategoryInviteResponseReqDto;
import com.soi.backend.domain.category.dto.CategoryRespDto;
import com.soi.backend.domain.category.entity.CategoryFilter;
import com.soi.backend.domain.category.service.CategoryService;
import com.soi.backend.domain.category.service.CategorySetService;
import com.soi.backend.domain.post.dto.PostRespDto;
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
    private final CategorySetService categorySetService;

    @Operation(summary = "카테고리 추가", description = "카테고리를 추가합니다.")
    @PostMapping("/create")
    public ResponseEntity<ApiResponseDto<Long>> create(@RequestBody CategoryCreateReqDto categoryCreateReqDto) {
        Long categoryId = categoryService.initializeCategory(categoryCreateReqDto);
        return ResponseEntity.ok(ApiResponseDto.success(categoryId, "카테고리 추가 완료"));
    }

    @Operation(summary = "카테고리 나가기 (삭제)", description = "카테고리를 나갑니다. (만약 카테고리에 속한 유저가 본인밖에 없으면 관련 데이터 다 삭제)")
    @PostMapping("/delete")
    public ResponseEntity<ApiResponseDto<?>> delete(@RequestParam Long userId, Long categoryId) {
        categoryService.deleteCategory(userId, categoryId);
        return ResponseEntity.ok(ApiResponseDto.success(null,"카테고리 나가기(삭제) 완료"));
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
    public ResponseEntity<ApiResponseDto<Boolean>> inviteResponse(@RequestBody CategoryInviteResponseReqDto categoryInviteResponseReqDto) {
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

    @Operation(summary = "카테고리 고정", description = "카테고리 아이디, 유저 아이디로 카테고리를 고정 혹은 고정해제 시킵니다.")
    @PostMapping("/set/pinned")
    public ResponseEntity<ApiResponseDto<Boolean>> categoryPinned(@RequestParam Long categoryId,
                                                                  @RequestParam Long userId) {
        Boolean result = categorySetService.setPinned(categoryId, userId);
        return ResponseEntity.ok(ApiResponseDto.success(result,"상단고정값 변경 완료"));
    }

    @Operation(summary = "카테고리 이름수정", description = "카테고리 아이디, 유저 아이디, 수정할 이름을 받아 카테고리 이름을 수정합니다.\n커스텀한 이름을 삭제하길 원하면 name에 그냥 빈값 \"\" 을 넣으면 커스텀 이름이 삭제됩니다.")
    @PostMapping("/set/name")
    public ResponseEntity<ApiResponseDto<Boolean>> customName(@RequestParam Long categoryId,
                                                              @RequestParam Long userId,
                                                              @RequestParam(required = false) String name) {
        Boolean result = categorySetService.setName(categoryId, userId, name);
        return ResponseEntity.ok(ApiResponseDto.success(result,"이름 변경 완료"));
    }

    @Operation(summary = "카테고리 프로필 수정", description = "카테고리 아이디, 유저 아이디, 수정할 프로필 사진을 받아 프로필을 수정합니다.\n기본 프로필로 변경하고싶으면 profileImageKey에 \"\" 을 넣으면 됩니다.")
    @PostMapping("/set/profile")
    public ResponseEntity<ApiResponseDto<Boolean>> customProfile(@RequestParam Long categoryId,
                                                                  @RequestParam Long userId,
                                                                 @RequestParam(required = false) String profileImageKey) {
        Boolean result = categorySetService.setProfile(categoryId, userId, profileImageKey);
        return ResponseEntity.ok(ApiResponseDto.success(result,"프로필 변경 완료"));
    }
}
