package com.soi.backend.auth.service;

import com.soi.backend.auth.entity.RefreshToken;
import com.soi.backend.auth.jwt.JwtProvider;
import com.soi.backend.auth.repository.RefreshTokenRepository;
import com.soi.backend.domain.user.entity.User;
import com.soi.backend.domain.user.repository.UserRepository;
import com.soi.backend.global.exception.CustomException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;

    @Transactional
    public String issue(Long userId, Integer sessionVersion) {
        refreshTokenRepository.deleteAllByUserId(userId);

        String refreshToken = jwtProvider.createRefreshToken(userId, sessionVersion);
        RefreshToken entity = new RefreshToken(
                userId,
                hash(refreshToken),
                LocalDateTime.now().plus(Duration.ofMillis(jwtProvider.getRefreshExpirationMs()))
        );
        refreshTokenRepository.save(entity);
        return refreshToken;
    }

    @Transactional
    public Long validateAndConsume(String refreshToken) {
        if (!jwtProvider.validateRefreshToken(refreshToken)) {
            throw new CustomException("유효하지 않은 refresh token 입니다.", HttpStatus.UNAUTHORIZED);
        }

        Long userId = jwtProvider.getUserIdFromRefreshToken(refreshToken);
        Integer sessionVersion = jwtProvider.getSessionVersionFromRefreshToken(refreshToken);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException("유저 정보를 찾을 수 없습니다.", HttpStatus.UNAUTHORIZED));

        if (!sessionVersion.equals(user.getSessionVersion())) {
            throw new CustomException("다른 기기에서 다시 로그인되어 세션이 만료되었습니다.", HttpStatus.UNAUTHORIZED);
        }

        RefreshToken savedToken = refreshTokenRepository.findByTokenHash(hash(refreshToken))
                .orElseThrow(() -> new CustomException("refresh token 정보를 찾을 수 없습니다.", HttpStatus.UNAUTHORIZED));

        if (!savedToken.getUserId().equals(userId)) {
            throw new CustomException("refresh token 정보가 일치하지 않습니다.", HttpStatus.UNAUTHORIZED);
        }

        if (savedToken.isRevoked() || savedToken.isExpired()) {
            throw new CustomException("이미 만료되었거나 폐기된 refresh token 입니다.", HttpStatus.UNAUTHORIZED);
        }

        refreshTokenRepository.deleteById(savedToken.getId());
        return userId;
    }

    @Transactional
    public void revoke(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return;
        }

        if (!jwtProvider.validateRefreshToken(refreshToken)) {
            return;
        }

        Long userId = jwtProvider.getUserIdFromRefreshToken(refreshToken);
        refreshTokenRepository.deleteAllByUserId(userId);
    }

    private String hash(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            for (byte value : hashed) {
                builder.append(String.format("%02x", value));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 hash 생성에 실패했습니다.", e);
        }
    }
}
