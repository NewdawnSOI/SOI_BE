package com.soi.backend.domain.post.repository;

import com.soi.backend.domain.post.entity.Post;
import com.soi.backend.domain.post.entity.PostStatus;
import com.soi.backend.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {
    @Query("""
    SELECT p, u
    FROM Post p
    JOIN User u ON p.userId = u.id
    WHERE p.categoryId = :categoryId
    AND p.status = com.soi.backend.domain.post.entity.PostStatus.ACTIVE
    AND p.isActive = true
    
    AND NOT EXISTS (
        SELECT 1
        FROM Friend f
        WHERE f.status = com.soi.backend.domain.friend.entity.FriendStatus.BLOCKED
          AND (
               (f.requesterId = :viewerId AND f.receiverId = p.userId)
            OR (f.requesterId = p.userId AND f.receiverId = :viewerId)
          )
    )
    
    ORDER BY p.createdAt DESC
    """)
    Page<Object[]> findCategoryPosts(
            @Param("categoryId") Long categoryId,
            @Param("viewerId") Long viewerId,
            Pageable pageable
    );

    @Query("""
    SELECT p, u
    FROM Post p
    JOIN User u ON p.userId = u.id
    WHERE p.categoryId IN :categoryIds
    AND p.status = :status
    AND p.isActive = :isActive
    
    AND NOT EXISTS (
        SELECT 1
        FROM Friend f
        WHERE f.status = com.soi.backend.domain.friend.entity.FriendStatus.BLOCKED
          AND (
               (f.requesterId = :viewerId AND f.receiverId = p.userId)
            OR (f.requesterId = p.userId AND f.receiverId = :viewerId)
          )
    )
    
    ORDER BY p.createdAt DESC
    """)
    Page<Object[]> findFeedPosts(
            @Param("categoryIds") List<Long> categoryIds,
            @Param("status") PostStatus status,
            @Param("isActive") boolean isActive,
            @Param("viewerId") Long viewerId,
            Pageable pageable
    );

    @Query("""
        SELECT p
        FROM Post p
        WHERE p.categoryId = :categoryId
    """)
    List<Post> findAllByCategoryId(Long categoryId);

    Optional<Post> findTopByCategoryIdAndStatusOrderByCreatedAtDesc(
            Long categoryId,
            PostStatus status
    );

    Post findByIdAndCategoryId(Long id, Long categoryId);

    @Query("""
    SELECT p, u
    FROM Post p
    JOIN User u ON p.userId = u.id
    WHERE p.categoryId IN :categoryIds
    AND p.status = :status
    AND p.isActive = :isActive
    ORDER BY p.createdAt DESC
    """)
    Optional<Object[]> findPostWithUser(@Param("postId") Long postId);
}
