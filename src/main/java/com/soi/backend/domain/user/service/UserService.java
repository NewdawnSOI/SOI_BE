package com.soi.backend.domain.user.service;

import com.soi.backend.external.sms.MessageService;
import com.soi.backend.domain.friend.dto.FriendReqDto;
import com.soi.backend.global.exception.CustomException;
import com.soi.backend.domain.user.dto.UserCreateReqDto;
import com.soi.backend.domain.user.dto.UserFindRespDto;
import com.soi.backend.domain.user.dto.UserRespDto;
import com.soi.backend.domain.user.entity.User;
import com.soi.backend.domain.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

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
            throw new CustomException("이미 존재하는 사용자입니다.", HttpStatus.CONFLICT);
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

    @Transactional
    public List<UserFindRespDto> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(user -> new UserFindRespDto(
                        user.getId(),
                        user.getName(),
                        user.getUserId(),
                        user.getProfileImage(),
                        user.isActive()
                ))
                .collect(Collectors.toList());
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
            throw new CustomException("로그인 에러 : 로그인 에러 : 해당 번호로 등록된 유저가 없습니다.", HttpStatus.NOT_FOUND);
        }
    }

    @Transactional
    public UserRespDto deleteUser(Long id) {
        if (userRepository.findById(id).isPresent()) {
            User user = userRepository.findById(id).get();
            userRepository.delete(user);
            return toDto(user);
        } else {
            throw new CustomException("삭제 하려는 사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND);
        }
    }

    public List<UserRespDto> findByUserId(String userId) {
        return userRepository.searchAllByUserId(escapeLikeKeyword(userId))
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private String escapeLikeKeyword(String keyword) {
        return keyword
                .replace("\\", "\\\\")
                .replace("_", "\\_")
                .replace("%", "\\%");
    }

    private UserRespDto toDto(User user) {
        return new UserRespDto(user.getId(), user.getUserId());
    }

    public Boolean checkUserExists(FriendReqDto friendReqDto) {
        return userRepository.findByIdAndIsActive(friendReqDto.getRequesterId()).isPresent()
                && userRepository.findByIdAndIsActive(friendReqDto.getReceiverId()).isPresent();
    }

}
