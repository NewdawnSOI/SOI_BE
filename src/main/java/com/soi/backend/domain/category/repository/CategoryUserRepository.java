package com.soi.backend.domain.category.repository;

import com.soi.backend.domain.category.entity.Category;
import com.soi.backend.domain.category.entity.CategoryUser;
import com.soi.backend.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;


public interface CategoryUserRepository extends JpaRepository<CategoryUser, Long> {
    @Query("SELECT cu.categoryId FROM CategoryUser cu WHERE cu.userId = :userId")
    List<Long> findCategoriesByUserId(@Param("userId") Long userId);

    Optional<CategoryUser> findByCategoryIdAndUserId(Long categoryId, Long userId);

    @Query("""
        SELECT u
        FROM CategoryUser cu
        JOIN User u ON cu.userId = u.id
        WHERE cu.categoryId = :categoryId
        AND u.id <> :userId
    """)
    List<User> findAllUsersByCategoryIdExceptUser(Long categoryId, Long userId);

    @Query("""
        SELECT u.id
        FROM CategoryUser cu
        JOIN User u ON cu.userId = u.id
        WHERE cu.categoryId = :categoryId
        AND u.id <> :userId
    """)
    List<Long> findAllUserIdsByCategoryIdExceptUser(Long categoryId, Long userId);

    Optional<CategoryUser> findByUserIdAndCategoryId(Long userId, Long categoryId);

    List<CategoryUser> findAllByCategoryId(Long categoryId);
}
