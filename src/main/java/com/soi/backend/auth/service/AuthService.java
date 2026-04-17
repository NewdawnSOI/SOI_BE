package com.soi.backend.auth.service;

import com.soi.backend.auth.dto.LoginReqDto;
import com.soi.backend.auth.dto.LoginRespDto;
import com.soi.backend.auth.jwt.JwtProvider;
import com.soi.backend.domain.user.entity.User;
import com.soi.backend.domain.user.repository.UserRepository;
import com.soi.backend.global.metrics.BusinessMetricsService;
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
    private final BusinessMetricsService businessMetricsService;

    @Transactional
    public LoginRespDto login(LoginReqDto loginReqDto) {
        User user = userRepository.findByPhoneNum(loginReqDto.getPhoneNum())
                .orElseThrow(() -> new CustomException("User Id를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        user.setLastLogin(LocalDateTime.now());
        user.rotateSession();

        String accessToken = jwtProvider.createAccessToken(user.getId(), user.getSessionVersion());
        String refreshToken = refreshTokenService.issue(user.getId(), user.getSessionVersion());

        businessMetricsService.increment("auth_login_success");

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
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException("유저 정보를 찾을 수 없습니다.", HttpStatus.UNAUTHORIZED));

        String newAccessToken = jwtProvider.createAccessToken(userId, user.getSessionVersion());
        String newRefreshToken = refreshTokenService.issue(userId, user.getSessionVersion());

        businessMetricsService.increment("auth_refresh_success");

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
        businessMetricsService.increment("auth_logout");
    }
}
