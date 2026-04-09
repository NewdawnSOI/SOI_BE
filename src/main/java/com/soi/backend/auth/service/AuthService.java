package com.soi.backend.auth.service;

import com.soi.backend.auth.dto.LoginReqDto;
import com.soi.backend.auth.dto.LoginRespDto;
import com.soi.backend.auth.jwt.JwtProvider;
import com.soi.backend.domain.user.entity.User;
import com.soi.backend.domain.user.repository.UserRepository;
import com.soi.backend.global.exception.CustomException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor

public class AuthService {

    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final RefreshTokenService refreshTokenService;

    @Transactional
    public LoginRespDto login(LoginReqDto loginReqDto) {
        User user = userRepository.findByNickname(loginReqDto.getNickname())
                .orElseThrow(() -> new CustomException("User Id를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        if (!user.getPhoneNum().equals(loginReqDto.getPhoneNum())) {
            throw new CustomException("유저정보가 일치하지 않습니다.", HttpStatus.FORBIDDEN);
        }

        user.setLastLogin(LocalDateTime.now());

        String accessToken = jwtProvider.createAccessToken(user.getId());
        String refreshToken = refreshTokenService.issue(user.getId());

        return new LoginRespDto(
                accessToken,
                refreshToken,
                jwtProvider.getAccessExpirationMs(),
                jwtProvider.getRefreshExpirationMs()
        );
    }

    @Transactional
    public LoginRespDto refresh(String refreshToken) {
        Long userId = refreshTokenService.validateAndConsume(refreshToken);

        String newAccessToken = jwtProvider.createAccessToken(userId);
        String newRefreshToken = refreshTokenService.issue(userId);

        return new LoginRespDto(
                newAccessToken,
                newRefreshToken,
                jwtProvider.getAccessExpirationMs(),
                jwtProvider.getRefreshExpirationMs()
        );
    }

    @Transactional
    public void logout(String refreshToken) {
        refreshTokenService.revoke(refreshToken);
    }
}
