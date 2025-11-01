package com.soi.backend.user.controller;

import com.soi.backend.user.dto.UserCreateReqDto;
import com.soi.backend.user.dto.UserRespDto;
import com.soi.backend.user.service.SMSAuthService;
import com.soi.backend.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
@Tag(name = "User API", description = "사용자 관리 API")

public class UserController {

    private final UserService userService;
    private final SMSAuthService smsAuthService;

    @Operation(summary = "사용자 생성", description = "새로운 사용자를 등록합니다.")
    @PostMapping("/create")
    public ResponseEntity<?> createUser(@RequestBody UserCreateReqDto UserCreateReqDto) {
        UserRespDto userRespDto = userService.createUser(UserCreateReqDto);
        if (userRespDto != null) {
            return ResponseEntity.ok(userRespDto);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("사용자 생성 실패");
        }
    }

    @Operation(summary = "사용자 로그인(전화번호로)", description = "인증이 완료된 전화번호로 로그인을 합니다.")
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestParam String phone) {
        UserRespDto userRespDto = userService.loginByPhone(phone);
        if (userRespDto != null) {
            return ResponseEntity.ok(userRespDto);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("로그인 실패");
        }
    }

    @Operation(summary = "전화번호 인증", description = "사용자가 입력한 전화번호로 인증을 발송합니다.")
    @PostMapping("/auth")
    public ResponseEntity<Boolean> authSMS(@RequestParam String phone) {
        return ResponseEntity.ok(smsAuthService.sendSMStoAuth(phone));
    }

    @Operation(summary = "사용자 id 중복 체크", description = "사용자 id 중복 체크합니다. 리턴값 : 중복 : false, 중복아님 : true")
    @PostMapping("/id-check")
    public ResponseEntity<?> idCheck(@RequestParam String userId) {
        Boolean isDup = userService.isDuplicateUserId(userId);
        if (isDup) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(userId + " id가 중복입니다.");
        }
        return ResponseEntity.ok(false);
    }
}
