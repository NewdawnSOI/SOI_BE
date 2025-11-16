package com.soi.backend.domain.user.repository;

import com.soi.backend.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUserId(String userId);
    Optional<User> findByPhone(String phone);

    @Query("""
        SELECT u FROM User u
        WHERE u.id = :Id AND u.isActive = true
    """)
    Optional<User> findByIdAndIsActive(@Param("Id")Long id);

    @Query("""
        SELECT u FROM User u
        WHERE u.userId LIKE %:keyword% ESCAPE '\\'
    """)
    List<User> searchAllByUserId(@Param("keyword") String keyword);

    @Query("""
        SELECT u.profileImage FROM User u
        WHERE u.userId = :Id
    """)
    List<User> findAllProfileImage(@Param("Id") Long userId);
}
