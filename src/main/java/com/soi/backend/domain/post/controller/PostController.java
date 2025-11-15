package com.soi.backend.domain.post.controller;

import com.soi.backend.domain.post.dto.PostCreateReqDto;
import com.soi.backend.domain.post.dto.PostRespDto;
import com.soi.backend.domain.post.dto.PostUpdateReqDto;
import com.soi.backend.domain.post.service.PostService;
import com.soi.backend.global.ApiResponseDto;
import com.soi.backend.global.exception.BaseController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/post")

@Tag(name = "post API", description = "게시물 관련 API")
public class PostController extends BaseController {

    private final PostService postService;

    @Operation(summary = "게시물 추가", description = "게시물을 추가합니다.")
    @PostMapping("/create")
    public ResponseEntity<ApiResponseDto<Boolean>> create(@RequestBody PostCreateReqDto postCreateReqDto) {
        try {
            Boolean categoryId = postService.addPostToCategory(postCreateReqDto);
            return ResponseEntity.ok(ApiResponseDto.success(categoryId,"게시물 추가 완료"));
        } catch (Exception e) {
            return handleExecption(e);
        }
    }

    @Operation(summary = "게시물 수정", description = "게시물을 수정합니다.")
    @PatchMapping("/update")
    public ResponseEntity<ApiResponseDto<Object>> update(@RequestBody PostUpdateReqDto postUpdateReqDto) {
        try {
            postService.updatePost(postUpdateReqDto);
            return ResponseEntity.ok(ApiResponseDto.success(null,"게시물 수정 완료"));
        } catch (Exception e) {
            return handleExecption(e);
        }

    }

    @Operation(summary = "게시물 삭제", description = "게시물을 삭제합니다. 삭제된건 일단 휴지통으로 이동됨")
    @DeleteMapping("/delete")
    public ResponseEntity<ApiResponseDto<Object>> delete(@RequestParam Long postId) {
        try {
            postService.softDeletePost(postId);
            return ResponseEntity.ok(ApiResponseDto.success(null,"게시물 임시삭제 완료"));
        } catch (Exception e) {
            return handleExecption(e);
        }
    }

    @Operation(summary = "카테고리에 해당하는 게시물 조회", description = "카테고리 아이디로 해당 카테고리에 속한 게시물을 조회합니다.")
    @GetMapping("/find-by/category")
    public ResponseEntity<ApiResponseDto<List<PostRespDto>>> findByCategoryId(@RequestParam Long categoryId) {
        try {
            List<PostRespDto> postRespDtos = postService.findByCategoryId(categoryId);
            return ResponseEntity.ok(ApiResponseDto.success(postRespDtos,"게시물 조회 완료"));
        } catch (Exception e) {
            return handleExecption(e);
        }
    }

    @Operation(summary = "메인페이지에 띄울 게시물 조회", description = "사용자가 포함된 카테고리의 모든 게시물을 리턴해줌")
    @GetMapping("/find-all")
    public ResponseEntity<ApiResponseDto<List<PostRespDto>>> findAllByUserId(@RequestParam Long userId) {
        try {
            List<PostRespDto> postRespDtos = postService.findPostToShowMainPage(userId);
            return ResponseEntity.ok(ApiResponseDto.success(postRespDtos,"전체 게시물 조회 완료"));
        } catch (Exception e) {
            return handleExecption(e);
        }
    }

    @Operation(summary = "단일 게시물 조회", description = "게시물 id로 해당 게시물의 상세정보를 조회합니다.")
    @GetMapping("/detail")
    public ResponseEntity<ApiResponseDto<PostRespDto>> showDetail(@RequestParam Long postId) {
        try {
            PostRespDto postRespDto = postService.showPostDetail(postId);
            return ResponseEntity.ok(ApiResponseDto.success(postRespDto,"게시물 조회 완료"));
        } catch (Exception e) {
            return handleExecption(e);
        }
    }
}
