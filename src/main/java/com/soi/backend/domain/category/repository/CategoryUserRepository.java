package com.soi.backend.domain.category.repository;

import com.soi.backend.domain.category.entity.CategoryUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;


public interface CategoryUserRepository extends JpaRepository<CategoryUser, Long> {
    @Query("SELECT cu.categoryId FROM CategoryUser cu WHERE cu.userId = :userId")
    List<Long> findCategoriesByUserId(@Param("userId") Long userId);
}
