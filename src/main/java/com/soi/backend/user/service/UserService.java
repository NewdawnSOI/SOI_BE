package com.soi.backend.user.service;

import com.soi.backend.external.sms.MessageService;
import com.soi.backend.user.dto.UserCreateReqDto;
import com.soi.backend.user.dto.UserRespDto;
import com.soi.backend.user.entity.User;
import com.soi.backend.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j

public class UserService {

    private final UserRepository userRepository;
    private final MessageService messageService;

    // 계정 생성
    @Transactional
    public UserRespDto createUser(UserCreateReqDto userCreateReqDto) {
        if (!isDuplicateUserId(userCreateReqDto.getUserId())
            || !isDuplicatePhone(userCreateReqDto.getPhone())) {
            log.error("사용자 생성 실패");
            return null;
        }

        User user = new User(
                userCreateReqDto.getName(),
                userCreateReqDto.getPhone(),
                userCreateReqDto.getUserId(),
                userCreateReqDto.getProfileImage(),
                userCreateReqDto.getBirth_date(),
                userCreateReqDto.getServiceAgreed(),
                userCreateReqDto.getPrivacyPolicyAgreed(),
                userCreateReqDto.getMarketingAgreed()
                );

        return toDto(userRepository.save(user));
    }

    // 계정 중복 체크
    public Boolean isDuplicateUserId(String userId) {
        if (userRepository.findByUserId(userId).isPresent()) {
            log.error("아이디 중복 체크 : 이미 존재하는 아이디 {}", userId);
            return false;
        } else {
            log.info("아이디 중복 체크 : 생성가능한 아이디 {}", userId);
            return true;
        }
    }

    // 전화번호 중복 체크 중복 : false, 가능 : true
    public Boolean isDuplicatePhone(String phone) {
        if (userRepository.findByPhone(phone).isPresent()) {
            log.error("아이디 중복 체크 : 이미 존재하는 전화번호 {}", phone);
            return false;
        } else {
            log.info("아이디 중복 체크 : 생성가능한 전화번호 {}", phone);
            return true;
        }
    }

    public UserRespDto loginByPhone(String phone) {
        if (userRepository.findByPhone(phone).isPresent()) {
            User user = userRepository.findByPhone(phone).get();
            return toDto(user);
        } else {
            log.error("로그인 에러 : 해당 번호로 등록된 유저가 없습니다. : {}", phone);
            return null;
        }
    }

    @Transactional
    public UserRespDto deleteUser(String userId) {
        if (userRepository.findByUserId(userId).isPresent()) {
            User user = userRepository.findByUserId(userId).get();
            userRepository.delete(user);
            return toDto(user);
        } else {
            return null;
        }
    }

    private UserRespDto toDto(User user) {
        return new UserRespDto(user.getId(), user.getUserId());
    }
}
