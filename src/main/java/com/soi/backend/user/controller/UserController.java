package com.soi.backend.user.controller;

import com.soi.backend.user.dto.UserCreateReqDto;
import com.soi.backend.user.dto.UserCreateRespDto;
import com.soi.backend.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
@Tag(name = "User API", description = "사용자 관리 API")

public class UserController {

    private final UserService userService;

    @Operation(summary = "사용자 생성", description = "새로운 사용자를 등록합니다.")
    @PostMapping("/create")
    public ResponseEntity<UserCreateRespDto> createUser(@RequestBody UserCreateReqDto UserCreateReqDto) {
        return ResponseEntity.ok(userService.createUser(UserCreateReqDto));
    }

//    @Operation(summary = "전화번호 인증", description = "사용자가 입력한 전화번호로 인증을 발송합니다.")
//    @PostMapping("/auth")
//    public ResponseEntity<Boolean>
}
