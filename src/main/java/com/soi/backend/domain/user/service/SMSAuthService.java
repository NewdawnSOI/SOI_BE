package com.soi.backend.domain.user.service;

import com.soi.backend.domain.user.dto.AuthCheckReqDto;
import com.soi.backend.external.sms.MessageService;
import com.soi.backend.domain.user.entity.SMSAuth;
import com.soi.backend.domain.user.repository.SMSAuthRepository;
import com.soi.backend.global.exception.CustomException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
    public Boolean sendSMStoAuth(String phoneNum) {
//        if (!userService.isDuplicatePhone(phoneNum)) {
//            return false;
//        } -> 인증 보낼때는 중복체크 X

        String verificationCode = UUID.randomUUID().toString().replace("-", "").substring(0, 10);

        messageService.sendMessage(verificationCode, phoneNum);
        smsAuthRepository.save(new SMSAuth(phoneNum, verificationCode));
        return true;
    }

    @Transactional
    public Boolean checkCode(AuthCheckReqDto authCheckReqDto) {
        final String phoneNum = authCheckReqDto.getPhoneNum();
        final String code = authCheckReqDto.getCode();
      
        String authCode = smsAuthRepository.findByPhone(phoneNum)
                .orElseThrow(() -> new CustomException("인증 요청을 보내지 않은 전화번호입니다.", HttpStatus.NOT_FOUND))
                .getVerificationCode();
        smsAuthRepository.deleteByPhone(phoneNum);
      
        return authCode.equals(code);
    }
}
