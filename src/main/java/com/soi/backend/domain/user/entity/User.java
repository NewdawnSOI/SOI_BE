package com.soi.backend.domain.user.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter

@Table(name = "user", schema = "soi")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String phone;

    @Column(name = "user_id",  nullable = false)
    private String userId;

    @Column(name = "profile_image")
    private String profileImage;

    @Column(name = "birth_date")
    private String birthDate;

    @Column(name = "is_active")
    private boolean isActive;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @Column(name = "service_agreed")
    private boolean serviceAgreed;

    @Column(name = "privacy_policy_agreed")
    private boolean privacyPolicyAgreed;

    @Column(name = "marketing_agreed")
    private boolean marketingAgreed;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }

    public User (String name, String phone, String userId,  String profileImage, String birthDate,
                 Boolean serviceAgreed, Boolean privacyPolicyAgreed, Boolean marketingAgreed) {
        this.name = name;
        this.phone = phone;
        this.userId = userId;
        this.profileImage = profileImage;
        this.birthDate = birthDate;
        this.isActive = true;
        this.createdAt = LocalDateTime.now();
        this.lastLogin = LocalDateTime.now();
        this.serviceAgreed = serviceAgreed;
        this.privacyPolicyAgreed = privacyPolicyAgreed;
        this.marketingAgreed = marketingAgreed;
    }
}
