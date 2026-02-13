package com.soi.backend.domain.comment.repository;

import com.soi.backend.domain.comment.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findAllByPostId(Long postId);

    @Query("""
        SELECT c.id
        FROM Comment c
        WHERE c.postId = :postId
    """)
    List<Long> findAllIdByPostId(Long postId);

    @Query("""
        SELECT c.postId, COUNT(c)
        FROM Comment c
        WHERE c.postId IN :postIds
        GROUP BY c.postId
    """)
    List<Object[]> countCommentByPostIds(@Param("postIds") List<Long> postIds);
}
