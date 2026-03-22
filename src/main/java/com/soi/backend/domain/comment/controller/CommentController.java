package com.soi.backend.domain.comment.controller;

import com.soi.backend.domain.category.dto.CategoryCreateReqDto;
import com.soi.backend.domain.comment.dto.CommentReqDto;
import com.soi.backend.domain.comment.dto.CommentRespDto;
import com.soi.backend.domain.comment.service.CommentService;
import com.soi.backend.global.ApiResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/comment")

@Tag(name = "comment API", description = "댓글 관련 API")
public class CommentController {

    private final CommentService commentService;

    @Operation(summary = "댓글 추가", description = "댓글을 추가합니다.")
    @PostMapping("/create")
    public ResponseEntity<ApiResponseDto<?>> create(@RequestBody CommentReqDto commentReqDto) {
        commentService.addComment(commentReqDto);
        return ResponseEntity.ok(
                ApiResponseDto.success(null,"댓글 추가 완료"));
    }

    @Operation(summary = "원댓글 조회", description = "게시물에 달린 댓글을 조회합니다.")
    @GetMapping("/get-parent")
    public ResponseEntity<ApiResponseDto<Slice<CommentRespDto>>> getParentComment(@RequestParam Long postId, @RequestParam int page) {
        Slice<CommentRespDto> commentRespDtos = commentService.getParentComments(postId, page);
        return ResponseEntity.ok(
                ApiResponseDto.success(commentRespDtos,"댓글 조회 완료"));
    }

    @Operation(summary = "대댓글 조회", description = "댓글에 달린 대댓글을 조회합니다.")
    @GetMapping("/get-child")
    public ResponseEntity<ApiResponseDto<Slice<CommentRespDto>>> getChildComment(@RequestParam Long parentCommentId, @RequestParam int page) {
        Slice<CommentRespDto> commentRespDtos = commentService.getChildComments(parentCommentId, page);
        return ResponseEntity.ok(
                ApiResponseDto.success(commentRespDtos,"대댓글 조회 완료"));
    }

    @Operation(summary = "사용자가 작성한 댓글 조회", description = "사용자가 작성한 모든 댓글을 조회합니다.")
    @GetMapping("/get/by-user-id")
    public ResponseEntity<ApiResponseDto<Slice<CommentRespDto>>> getAllCommentByUserId(@AuthenticationPrincipal Long userId, @RequestParam int page) {
        Slice<CommentRespDto> commentRespDtos = commentService.getAllCommentByUserId(userId, page);
        return ResponseEntity.ok(
                ApiResponseDto.success(commentRespDtos,"대댓글 조회 완료"));
    }

    @Operation(summary = "댓글 삭제", description = "id를 통해서 댓글을 삭제합니다.")
    @DeleteMapping("/delete")
    public ResponseEntity<ApiResponseDto<List<?>>> deleteComment(@RequestParam Long commentId) {
        commentService.deleteComment(commentId);
        return ResponseEntity.ok(
                ApiResponseDto.success(null,"댓글 삭제 완료"));
    }
}
