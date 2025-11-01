package com.soi.backend.user.controller;

import com.soi.backend.global.ApiResponseDto;
import com.soi.backend.global.exception.BaseController;
import com.soi.backend.user.dto.UserCreateReqDto;
import com.soi.backend.user.dto.UserFindRespDto;
import com.soi.backend.user.dto.UserRespDto;
import com.soi.backend.user.entity.User;
import com.soi.backend.user.service.SMSAuthService;
import com.soi.backend.user.service.UserService;
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

public class UserController extends BaseController {

    private final UserService userService;
    private final SMSAuthService smsAuthService;

    @Operation(summary = "사용자 생성", description = "새로운 사용자를 등록합니다.")
    @PostMapping("/create")
    public ResponseEntity<ApiResponseDto<UserRespDto>> createUser(@RequestBody UserCreateReqDto UserCreateReqDto) {
        try {
            UserRespDto userRespDto = userService.createUser(UserCreateReqDto);
            return ResponseEntity.ok(ApiResponseDto.success(userRespDto,"사용자 생성 성공"));
        } catch (Exception e) {
            return handleExecption(e);
        }
    }

    @Operation(summary = "모든유저 조회", description = "모든유저를 조회합니다.")
    @GetMapping("/get-all")
    public ResponseEntity<ApiResponseDto<List<UserFindRespDto>>> getAllUsers() {
        List<UserFindRespDto> users = userService.getAllUsers();
        return ResponseEntity.ok(ApiResponseDto.success(users, "모든 사용자 조회 완료"));
    }

    @Operation(summary = "사용자 로그인(전화번호로)", description = "인증이 완료된 전화번호로 로그인을 합니다.")
    @PostMapping("/login")
    public ResponseEntity<ApiResponseDto<UserRespDto>> login(@RequestParam String phone) {
        try {
            UserRespDto userRespDto = userService.loginByPhone(phone);
            return ResponseEntity.ok(ApiResponseDto.success(userRespDto, "로그인 성공"));
        } catch (Exception e) {
            return handleExecption(e);
        }
    }

    // SMS 전송 서비스 찾을때까지 사용 X
    @Operation(summary = "전화번호 인증", description = "사용자가 입력한 전화번호로 인증을 발송합니다.")
    @PostMapping("/auth")
    public ResponseEntity<Boolean> authSMS(@RequestParam String phone) {
        return ResponseEntity.ok(smsAuthService.sendSMStoAuth(phone));
    }

    @Operation(summary = "사용자 id 중복 체크", description = "사용자 id 중복 체크합니다. 사용가능 : true, 사용불가(중복) : false")
    @GetMapping("/id-check")
    public ResponseEntity<ApiResponseDto<Boolean>> idCheck(@RequestParam String userId) {
        Boolean isDup = userService.isDuplicateUserId(userId);
        if (isDup) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponseDto.fail(userId + " id가 중복입니다."));
        }
        return ResponseEntity.ok(ApiResponseDto.success(true, "사용가능한 id입니다."));
    }

    @Operation(summary = "유저 Id로 사용자 삭제", description = "id 로 사용자를 삭제합니다.")
    @DeleteMapping("/delete")
    public ResponseEntity<ApiResponseDto<UserRespDto>> deleteUser(@RequestParam String userId) {
        try {
            UserRespDto userRespDto = userService.deleteUser(userId);
            return ResponseEntity.ok(ApiResponseDto.success(userRespDto,"유저 삭제 성공"));
        } catch (Exception e) {
            return handleExecption(e);
        }
    }
}
