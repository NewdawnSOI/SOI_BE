package com.soi.backend.domain.user.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
    private String verificationCode;
}
