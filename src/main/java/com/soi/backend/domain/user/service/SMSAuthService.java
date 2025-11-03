package com.soi.backend.domain.user.service;

import com.soi.backend.external.sms.MessageService;
import com.soi.backend.domain.user.entity.SMSAuth;
import com.soi.backend.domain.user.repository.SMSAuthRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor

public class SMSAuthService {
    private final SMSAuthRepository smsAuthRepository;
    private final UserService userService;
    private final MessageService messageService;

    // 전화번호 인증 메시지 보내기
    @Transactional
    public Boolean sendSMStoAuth(String phone) {
        if (!userService.isDuplicatePhone(phone)) {
            return false;
        }

        String verificationCode = UUID.randomUUID().toString().replace("-", "").substring(0, 10);

        messageService.sendMessage(verificationCode, phone);
        smsAuthRepository.save(new SMSAuth(phone, verificationCode));
        return true;
    }
}
