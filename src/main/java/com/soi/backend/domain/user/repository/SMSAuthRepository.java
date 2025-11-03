package com.soi.backend.domain.user.repository;

import com.soi.backend.domain.user.entity.SMSAuth;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SMSAuthRepository extends JpaRepository<SMSAuth, String> {
}
