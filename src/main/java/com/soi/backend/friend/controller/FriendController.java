package com.soi.backend.friend.controller;

import com.soi.backend.friend.dto.FriendReqDto;
import com.soi.backend.friend.dto.FriendRespDto;
import com.soi.backend.friend.dto.FriendUpdateRespDto;
import com.soi.backend.friend.entity.Friend;
import com.soi.backend.friend.service.FriendService;
import com.soi.backend.global.ApiResponseDto;
import com.soi.backend.global.exception.BaseController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/friend")

@Tag(name = "friend API", description = "친구 관리 API")
public class FriendController extends BaseController {

    private final FriendService friendService;

    @Operation(summary = "친구 추가", description = "사용자 id를 통해 친구추가를 합니다.")
    @PostMapping("/create")
    public ResponseEntity<ApiResponseDto<FriendRespDto>> create(@RequestBody FriendReqDto friendReqDto) {
        try {
            FriendRespDto friendRespDto = friendService.createFriendRequest(friendReqDto);
            return ResponseEntity.ok(ApiResponseDto.success(friendRespDto,"친구 요청 성공"));
        } catch (Exception e) {
            return handleExecption(e);
        }
    }

    @Operation(summary = "친구 상태 업데이트", description = "친구 관계 id, 상태 : ACCEPTED, BLOCKED, CANCELLED 를 받아 상태를 업데이트합니다.")
    @PostMapping("/update")
    public ResponseEntity<ApiResponseDto<FriendRespDto>> update(@RequestBody FriendUpdateRespDto friendUpdateRespDto) {
        try {
            FriendRespDto friendRespDto = friendService.updateFriendRequest(friendUpdateRespDto);
            return ResponseEntity.ok(ApiResponseDto.success(friendRespDto, "친구 상태 업데이트 성공"));
        } catch (Exception e) {
            return handleExecption(e);
        }
    }
}
