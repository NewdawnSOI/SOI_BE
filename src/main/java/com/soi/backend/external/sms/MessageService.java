package com.soi.backend.external.sms;

import com.solapi.sdk.SolapiClient;
import com.solapi.sdk.message.exception.SolapiMessageNotReceivedException;
import com.solapi.sdk.message.model.Message;
import com.solapi.sdk.message.service.DefaultMessageService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j

public class MessageService {
    @Value("${sms.service.key}")
    private String api_key;
    @Value("${sms.service.secret}")
    private String api_secret;

    public void sendMessage(String verificationCode, String phone) {

        DefaultMessageService messageService =  SolapiClient.INSTANCE.createInstance(api_key, api_secret);
        Message message = new Message();
        message.setFrom("010-6651-5709");
        message.setTo(phone);
        message.setText("[SOI] 본인확인 인증번호 " + verificationCode + "을 입력해주세요");

        try {
            messageService.send(message);
        } catch (SolapiMessageNotReceivedException exception) {
            // 발송에 실패한 메시지 목록
            log.error(exception.getFailedMessageList().toString());
            log.error(exception.getMessage());
        } catch (Exception exception) {
            log.error(exception.getMessage());
        }

    }
}
