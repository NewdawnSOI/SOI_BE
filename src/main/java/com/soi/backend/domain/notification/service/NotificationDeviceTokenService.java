package com.soi.backend.domain.notification.service;

import com.soi.backend.domain.notification.dto.NotificationDeleteTokenReqDto;
import com.soi.backend.domain.notification.dto.NotificationRegisterTokenReqDto;
import com.soi.backend.domain.notification.entity.UserDeviceToken;
import com.soi.backend.domain.notification.repository.UserDeviceTokenRepository;
import com.soi.backend.global.exception.CustomException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationDeviceTokenService {

    private final UserDeviceTokenRepository userDeviceTokenRepository;

    @Transactional
    public void register(Long userId, NotificationRegisterTokenReqDto requestDto) {
        validateRegisterRequest(requestDto);

        UserDeviceToken userDeviceToken = userDeviceTokenRepository.findByToken(requestDto.getToken())
                .map(existingToken -> {
                    existingToken.bind(userId, requestDto.getPlatform());
                    return existingToken;
                })
                .orElseGet(() -> new UserDeviceToken(userId, requestDto.getToken(), requestDto.getPlatform()));

        userDeviceTokenRepository.save(userDeviceToken);
    }

    @Transactional
    public void delete(Long userId, NotificationDeleteTokenReqDto requestDto) {
        validateDeleteRequest(requestDto);

        userDeviceTokenRepository.findByToken(requestDto.getToken())
                .ifPresent(userDeviceToken -> {
                    if (!userDeviceToken.getUserId().equals(userId)) {
                        throw new CustomException("다른 사용자의 디바이스 토큰입니다.", HttpStatus.FORBIDDEN);
                    }
                    userDeviceToken.deactivate();
                });
    }

    @Transactional
    public void disable(String token) {
        userDeviceTokenRepository.findByToken(token)
                .ifPresent(UserDeviceToken::deactivate);
    }

    private void validateRegisterRequest(NotificationRegisterTokenReqDto requestDto) {
        if (requestDto == null || requestDto.getToken() == null || requestDto.getToken().isBlank()) {
            throw new CustomException("FCM 토큰이 필요합니다.");
        }

        if (requestDto.getPlatform() == null) {
            throw new CustomException("디바이스 플랫폼이 필요합니다.");
        }
    }

    private void validateDeleteRequest(NotificationDeleteTokenReqDto requestDto) {
        if (requestDto == null || requestDto.getToken() == null || requestDto.getToken().isBlank()) {
            throw new CustomException("FCM 토큰이 필요합니다.");
        }
    }
}
