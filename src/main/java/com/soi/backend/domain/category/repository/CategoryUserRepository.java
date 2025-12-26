package com.soi.backend.domain.category.repository;

import com.soi.backend.domain.category.entity.Category;
import com.soi.backend.domain.category.entity.CategoryUser;
import com.soi.backend.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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
    """)
    List<User> findAllUsersByCategoryId(Long categoryId);

    @Query("""
        SELECT cu
        FROM CategoryUser cu
        JOIN User u ON cu.userId = u.id
        WHERE cu.categoryId = :categoryId
        AND u.id <> :userId
    """)
    List<CategoryUser> findAllByCategoryIdExceptUser(Long categoryId, Long userId);

    Optional<CategoryUser> findByUserIdAndCategoryId(Long userId, Long categoryId);

    List<CategoryUser> findAllByCategoryId(Long categoryId);


    // 게시물 삭제될 때, 해당 게시물의 사진을 커스텀 프로필 사진으로 지정해놓은경우 지워지도록
    @Modifying
    @Query("""
    UPDATE CategoryUser cu
    SET cu.customProfile = null
    WHERE cu.categoryId = :categoryId
      AND cu.customProfile = :fileKey
""")
    int clearCustomProfileByCategoryIdAndFileKey(
            Long categoryId,
            String fileKey
    );
}
