package com.soi.backend.user.service;

import com.soi.backend.user.dto.UserCreateReqDto;
import com.soi.backend.user.dto.UserCreateRespDto;
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

    // 계정 생성
    public UserCreateRespDto createUser(UserCreateReqDto userCreateReqDto) {
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
        log.info("{} 아이디 중복 체크", userId);
        if (userRepository.findByUserId(userId).isPresent()) {
            log.error("이미 존재하는 아이디 {}", userId);
            return false;
        } else {
            log.info("생성가능한 아이디 {}", userId);
            return true;
        }
    }

    // 전화번호 중복 체크
    public Boolean isDuplicatePhone(String phone) {
        log.info("{} 전화번호 중복 체크", phone);
        if (userRepository.findByPhone(phone).isPresent()) {
            log.error("이미 존재하는 전화번호 {}", phone);
            return false;
        } else {
            log.info("생성가능한 전화번호 {}", phone);
            return true;
        }
    }

    private UserCreateRespDto toDto(User user) {
        return new UserCreateRespDto(user.getId(), user.getUserId());
    }
}
