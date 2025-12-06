package com.soi.backend.domain.category.repository;

import com.soi.backend.domain.category.entity.CategoryInvite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CategoryInviteRepository extends JpaRepository<CategoryInvite, Long> {
    Optional<CategoryInvite> findByCategoryIdAndInvitedUserId(Long categoryId, Long invitedUserId);

    @Query("""
        SELECT c.id
        FROM CategoryInvite c
        WHERE c.invitedUserId = :userId AND c.categoryId = :categoryId
    """)
    Optional<Long> findIdByCategoryIdAndUserId(Long userId, Long categoryId);
}
