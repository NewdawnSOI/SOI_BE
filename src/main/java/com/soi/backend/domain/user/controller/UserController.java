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
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
@Tag(name = "User API", description = "사용자 관리 API")

public class UserController {

    private final UserService userService;
    private final SMSAuthService smsAuthService;

    @Operation(summary = "사용자 생성", description = "새로운 사용자를 등록합니다.")
    @PostMapping("/create")
    public ResponseEntity<ApiResponseDto<UserRespDto>> createUser(@RequestBody UserCreateReqDto UserCreateReqDto) {
        UserRespDto userRespDto = userService.createUser(UserCreateReqDto);
        return ResponseEntity.ok(ApiResponseDto.success(userRespDto,"사용자 생성 성공"));
    }

    @Operation(summary = "모든유저 조회", description = "모든유저를 조회합니다.")
    @GetMapping("/get-all")
    public ResponseEntity<ApiResponseDto<List<UserFindRespDto>>> getAllUsers() {
        List<UserFindRespDto> users = userService.getAllUsers();
        return ResponseEntity.ok(ApiResponseDto.success(users, "모든 사용자 조회 완료"));
    }

    @Operation(summary = "특정유저 조회", description = "유저의 id값(Long)으로 유저를 조회합니다.")
    @GetMapping("/get")
    public ResponseEntity<ApiResponseDto<UserRespDto>> getUser(@RequestParam Long id) {
        UserRespDto user = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponseDto.success(user, "사용자 조회 완료"));
    }

    @Operation(summary = "사용자 로그인(전화번호로)", description = "인증이 완료된 전화번호로 로그인을 합니다.")
    @PostMapping("/login")
    public ResponseEntity<ApiResponseDto<UserRespDto>> login(@RequestParam String phone) {
        UserRespDto userRespDto = userService.loginByPhone(phone);
        return ResponseEntity.ok(ApiResponseDto.success(userRespDto, "로그인 성공"));
    }

    // SMS 전송 서비스 찾을때까지 사용 X
    @Operation(summary = "전화번호 인증", description = "사용자가 입력한 전화번호로 인증을 발송합니다.")
    @PostMapping("/auth")
    public ResponseEntity<Boolean> authSMS(@RequestParam String phone) {
        return ResponseEntity.ok(smsAuthService.sendSMStoAuth(phone));
    }

    @Operation(summary = "전화번호 인증확인", description = "사용자 전화번호와 사용자가 입력한 인증코드를 보내서 인증확인을 진행합니다.")
    @PostMapping("/auth/check")
    public ResponseEntity<Boolean> checkAuthSMS(@RequestBody AuthCheckReqDto authCheckReqDto) {
        return ResponseEntity.ok(smsAuthService.checkCode(authCheckReqDto));
    }

    @Operation(summary = "사용자 id 중복 체크", description = "사용자 id 중복 체크합니다. 사용가능 : true, 사용불가(중복) : false")
    @GetMapping("/id-check")
    public ResponseEntity<ApiResponseDto<Boolean>> idCheck(@RequestParam String userId) {
        Boolean isDup = userService.isDuplicateUserId(userId);
        if (!isDup) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponseDto.fail(" id가 중복입니다."));
        }
        return ResponseEntity.ok(ApiResponseDto.success(true, "사용가능한 id입니다."));
    }

    @Operation(summary = "Id로 사용자 삭제", description = "Id 로 사용자를 삭제합니다.")
    @DeleteMapping("/delete")
    public ResponseEntity<ApiResponseDto<UserRespDto>> deleteUser(@RequestParam Long id) {
        UserRespDto userRespDto = userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponseDto.success(userRespDto,"유저 삭제 성공"));
    }

    @Operation(summary = "키워드로 사용자 검색", description = "키워드가 포함된 userId를 갖고있는 사용자를 전부 검색합니다.")
    @GetMapping("/find-by-keyword")
    public ResponseEntity<ApiResponseDto<List<UserRespDto>>> findUser(@RequestParam String userId) {
        List<UserRespDto> userRespDtos = userService.findByUserId(userId);
        return ResponseEntity.ok(ApiResponseDto.success(userRespDtos, "키워드가 포함된 사용자 검색 성공"));
    }

    @Operation(summary = "유저정보 업데이트", description = "새로운 데이터로 유저정보를 업데이트합니다.")
    @PatchMapping("/update")
    public ResponseEntity<ApiResponseDto<UserRespDto>> update(@RequestBody UserUpdateReqDto userUpdateReqDto) {
        UserRespDto userRespDto = userService.update(userUpdateReqDto);
        return ResponseEntity.ok(ApiResponseDto.success(userRespDto, "유저 정보 업데이트 성공"));
    }

    @Operation(summary = "유저 프로필 업데이트", description = "유저의 프로필을 업데이트 합니다.")
    @PatchMapping("/update-profile")
    public ResponseEntity<ApiResponseDto<UserRespDto>> updateProfile(@RequestParam Long userId,
                                                                     @RequestParam String profileImage) {
        UserRespDto userRespDto = userService.updateUserProfile(userId, profileImage);
        return ResponseEntity.ok(ApiResponseDto.success(userRespDto, "키워드가 포함된 사용자 검색 성공"));
    }

}
