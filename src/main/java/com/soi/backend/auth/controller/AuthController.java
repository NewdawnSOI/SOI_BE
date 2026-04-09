package com.soi.backend.auth.controller;

import com.soi.backend.auth.dto.LoginReqDto;
import com.soi.backend.auth.dto.LoginRespDto;
import com.soi.backend.auth.dto.RefreshTokenReqDto;
import com.soi.backend.auth.service.AuthService;
import com.soi.backend.domain.user.dto.AuthCheckReqDto;
import com.soi.backend.domain.user.dto.UserCreateReqDto;
import com.soi.backend.domain.user.dto.UserRespDto;
import com.soi.backend.domain.user.service.SMSAuthService;
import com.soi.backend.domain.user.service.UserService;
import com.soi.backend.global.ApiResponseDto;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;
    private final UserService userService;
    private final SMSAuthService smsAuthService;

    @PostMapping("/login")
    public LoginRespDto login(@RequestBody LoginReqDto loginReqDto) {
        return authService.login(loginReqDto);
    }

    @PostMapping("/refresh")
    public LoginRespDto refresh(@Valid @RequestBody RefreshTokenReqDto refreshTokenReqDto) {
        return authService.refresh(refreshTokenReqDto.getRefreshToken());
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponseDto<Boolean>> logout(@Valid @RequestBody RefreshTokenReqDto refreshTokenReqDto) {
        authService.logout(refreshTokenReqDto.getRefreshToken());
        return ResponseEntity.ok(ApiResponseDto.success(true, "로그아웃 완료"));
    }

    @Operation(summary = "사용자 생성", description = "새로운 사용자를 등록합니다.")
    @PostMapping("/signup")
    public ResponseEntity<ApiResponseDto<UserRespDto>> createUser(@RequestBody UserCreateReqDto UserCreateReqDto) {
        UserRespDto userRespDto = userService.createUser(UserCreateReqDto);
        return ResponseEntity.ok(ApiResponseDto.success(userRespDto,"사용자 생성 성공"));
    }

    @Operation(summary = "전화번호 인증", description = "사용자가 입력한 전화번호로 인증을 발송합니다.")
    @PostMapping("/sms")
    public ResponseEntity<Boolean> authSMS(@RequestParam String phoneNum) {
        return ResponseEntity.ok(smsAuthService.sendSMStoAuth(phoneNum
        ));
    }

    @Operation(summary = "전화번호 인증확인", description = "사용자 전화번호와 사용자가 입력한 인증코드를 보내서 인증확인을 진행합니다.")
    @PostMapping("/sms/check")
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
}
