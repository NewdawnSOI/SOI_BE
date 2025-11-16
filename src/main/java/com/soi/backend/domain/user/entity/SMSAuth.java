package com.soi.backend.domain.user.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor

@Table(name = "sms_auth", schema = "soi")
public class SMSAuth {
    @Id
    private String phone;

    @Column(name = "verification_code", nullable = false)
    private String verificationCode;
}
