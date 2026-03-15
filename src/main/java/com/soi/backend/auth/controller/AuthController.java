package com.soi.backend.auth.controller;

import com.soi.backend.auth.dto.LoginReqDto;
import com.soi.backend.auth.dto.LoginRespDto;
import com.soi.backend.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public LoginRespDto login(@RequestBody LoginReqDto loginReqDto) {
        return authService.login(loginReqDto);
    }
}
