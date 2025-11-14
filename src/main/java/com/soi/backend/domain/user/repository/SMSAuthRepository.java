package com.soi.backend.domain.user.repository;

import com.soi.backend.domain.user.entity.SMSAuth;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SMSAuthRepository extends JpaRepository<SMSAuth, String> {
    Optional<SMSAuth> findByPhoneId(String phoneId);
    Optional<SMSAuth> deleteByPhoneId(String phoneId);
}
