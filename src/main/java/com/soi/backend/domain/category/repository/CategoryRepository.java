package com.soi.backend.domain.category.repository;

import com.soi.backend.domain.category.entity.Category;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    @Query("SELECT cu.categoryId FROM CategoryUser cu WHERE cu.userId = :userId")
    List<Long> findCategoriesByUserId(@Param("userId") Long userId);

    @Query("""
        SELECT c.id
        FROM CategoryUser cu
        JOIN Category c ON cu.categoryId = c.id
        WHERE cu.userId = :userId
        AND (:isPublic IS NULL OR c.isPublic = :isPublic)
    """)
    List<Long> findCategoriesByUserIdAndPublicFilter(
            Long userId,
            Boolean isPublic,
            Pageable pageable
    );
}
