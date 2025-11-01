package com.soi.backend.user.service;

import com.soi.backend.external.sms.MessageService;
import com.soi.backend.user.entity.SMSAuth;
import com.soi.backend.user.repository.SMSAuthRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
@RequiredArgsConstructor

public class SMSAuthService {
    private final SMSAuthRepository smsAuthRepository;
    private final UserService userService;
    private final MessageService messageService;

    // 전화번호 인증 메시지 보내기
    @Transactional
    public Boolean sendSMStoAuth(String phone) {
        if (userService.isDuplicatePhone(phone)) {
            return false;
        }

        StringBuilder verificationCode = new StringBuilder();
        Random random = new Random();

        for(int i=0; i<10; i++) {
            verificationCode.append(random);
        }

        messageService.sendMessage(verificationCode.toString(), phone);
        smsAuthRepository.save(new SMSAuth(phone, verificationCode.toString()));
        return true;
    }
}
