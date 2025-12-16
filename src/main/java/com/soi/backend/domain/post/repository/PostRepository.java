package com.soi.backend.domain.post.repository;

import com.soi.backend.domain.post.entity.Post;
import com.soi.backend.domain.post.entity.PostStatus;
import com.soi.backend.domain.user.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findAllByCategoryIdAndStatusAndIsActiveOrderByCreatedAtDesc(Long categoryId,
                                                                           PostStatus status,
                                                                           Boolean isActive,
                                                                           Pageable pageable);
    List<Post> findAllByCategoryIdInAndStatusAndIsActiveOrderByCreatedAtDesc(List<Long> categoryId,
                                                                           PostStatus status,
                                                                           Boolean isActive,
                                                                             Pageable pageable);
    @Query("""
        SELECT p
        FROM Post p
        WHERE p.categoryId = :categoryId
    """)
    List<Post> findAllByCategoryId(Long categoryId);
}
