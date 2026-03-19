package com.soi.backend.domain.user.controller;

import com.soi.backend.domain.user.dto.*;
import com.soi.backend.global.ApiResponseDto;
import com.soi.backend.domain.user.service.SMSAuthService;
import com.soi.backend.domain.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
@Tag(name = "User API", description = "사용자 관리 API")

public class UserController {

    private final UserService userService;
    private final SMSAuthService smsAuthService;

    @Operation(summary = "모든유저 조회", description = "모든유저를 조회합니다.")
    @GetMapping("/get-all")
    public ResponseEntity<ApiResponseDto<List<UserFindRespDto>>> getAllUsers() {
        List<UserFindRespDto> users = userService.getAllUsers();
        return ResponseEntity.ok(ApiResponseDto.success(users, "모든 사용자 조회 완료"));
    }

    @Operation(summary = "특정유저 조회", description = "유저의 id값(Long)으로 유저를 조회합니다.")
    @GetMapping("/get")
    public ResponseEntity<ApiResponseDto<UserRespDto>> getUser(@AuthenticationPrincipal Long userId) {
        UserRespDto user = userService.getUserById(userId);
        return ResponseEntity.ok(ApiResponseDto.success(user, "사용자 조회 완료"));
    }

    @Operation(summary = "사용자 로그인(전화번호로)", description = "인증이 완료된 전화번호로 로그인을 합니다.")
    @PostMapping("/login/by-phone")
    public ResponseEntity<ApiResponseDto<UserRespDto>> loginByPhone(@RequestParam String phoneNum) {
        UserRespDto userRespDto = userService.loginByPhone(phoneNum);
        return ResponseEntity.ok(ApiResponseDto.success(userRespDto, "로그인 성공"));
    }

    @Operation(summary = "사용자 로그인(전화번호로)", description = "인증이 완료된 전화번호로 로그인을 합니다.")
    @PostMapping("/login/by-nickname")
    public ResponseEntity<ApiResponseDto<UserRespDto>> loginByNickname(@RequestParam String nickName) {
        UserRespDto userRespDto = userService.loginByNickname(nickName);
        return ResponseEntity.ok(ApiResponseDto.success(userRespDto, "로그인 성공"));
    }

    @Operation(summary = "Id로 사용자 삭제", description = "Id 로 사용자를 삭제합니다.")
    @DeleteMapping("/delete")
    public ResponseEntity<ApiResponseDto<UserRespDto>> deleteUser(@RequestParam Long id) {
        UserRespDto userRespDto = userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponseDto.success(userRespDto,"유저 삭제 성공"));
    }

    @Operation(summary = "키워드로 사용자 검색", description = "키워드가 포함된 userId를 갖고있는 사용자를 전부 검색합니다.")
    @GetMapping("/find-by-keyword")
    public ResponseEntity<ApiResponseDto<List<UserRespDto>>> findUser(@RequestParam String nickname) {
        List<UserRespDto> userRespDtos = userService.findByUserId(nickname);
        return ResponseEntity.ok(ApiResponseDto.success(userRespDtos, "키워드가 포함된 사용자 검색 성공"));
    }

    @Operation(summary = "유저정보 업데이트", description = "새로운 데이터로 유저정보를 업데이트합니다.")
    @PatchMapping("/update")
    public ResponseEntity<ApiResponseDto<UserRespDto>> update(@AuthenticationPrincipal Long userId,
                                                              @RequestBody UserUpdateReqDto userUpdateReqDto) {
        UserRespDto userRespDto = userService.update(userId, userUpdateReqDto);
        return ResponseEntity.ok(ApiResponseDto.success(userRespDto, "유저 정보 업데이트 성공"));
    }

    @Operation(summary = "유저 프로필 업데이트", description = "유저의 프로필을 업데이트 합니다.\n기본 프로필로 변경하고싶으면 profileImageKey에 \"\" 을 넣으면 됩니다.")
    @PatchMapping("/update-profile")
    public ResponseEntity<ApiResponseDto<UserRespDto>> updateProfile(@AuthenticationPrincipal Long userId,
                                                                     @RequestParam(required = false) String profileImageKey) {
        UserRespDto userRespDto = userService.updateUserProfile(userId, profileImageKey);
        return ResponseEntity.ok(ApiResponseDto.success(userRespDto, "유저 프로필 업데이트 성공"));
    }

    @Operation(summary = "유저 배경사진 업데이트", description = "유저의 배경사진을 업데이트 합니다.\n기본 배경화면으로 변경하고싶으면 profileImageKey에 \"\" 을 넣으면 됩니다.")
    @PatchMapping("/update-cover-image")
    public ResponseEntity<ApiResponseDto<UserRespDto>> updateCoverImage(@AuthenticationPrincipal Long userId,
                                                                     @RequestParam(required = false) String coverImageKey) {
        UserRespDto userRespDto = userService.updateCoverImage(userId, coverImageKey);
        return ResponseEntity.ok(ApiResponseDto.success(userRespDto, "유저 프로필 배경사진 업데이트 성공"));
    }

}
