package com.soi.backend.auth.service;

import com.soi.backend.auth.dto.LoginReqDto;
import com.soi.backend.auth.dto.LoginRespDto;
import com.soi.backend.auth.jwt.JwtProvider;
import com.soi.backend.domain.user.entity.User;
import com.soi.backend.domain.user.repository.UserRepository;
import com.soi.backend.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor

public class AuthService {

    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;

    public LoginRespDto login(LoginReqDto loginReqDto) {
        User user = userRepository.findByNickname(loginReqDto.getNickname())
                .orElseThrow(() -> new CustomException("User Id를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        if (!user.getPhoneNum().equals(loginReqDto.getPhoneNum())) {
            throw new CustomException("유저정보가 일치하지 않습니다.", HttpStatus.FORBIDDEN);
        }

        String token = jwtProvider.createToken(user.getId());

        return new LoginRespDto(token);
    }
}
