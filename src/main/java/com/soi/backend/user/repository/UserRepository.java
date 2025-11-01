package com.soi.backend.user.repository;

import com.soi.backend.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUserId(String userId);
    Optional<User> findByPhone(String phone);

    @Query("""
    SELECT u FROM User u
    WHERE u.id = :Id AND u.isActive = true
    """)
    Optional<User> findByIdAndIsActive(@Param("Id")Long id);
}
