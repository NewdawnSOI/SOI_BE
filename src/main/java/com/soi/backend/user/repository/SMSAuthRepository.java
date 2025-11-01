package com.soi.backend.user.repository;

import com.soi.backend.user.entity.SMSAuth;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SMSAuthRepository extends JpaRepository<SMSAuth, String> {
}
