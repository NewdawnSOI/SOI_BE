package com.soi.backend.domain.comment.repository;

import com.soi.backend.domain.comment.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findAllByPostId(Long postId);

    @Query("""
        SELECT c.id
        FROM Comment c
        WHERE c.postId = :postId
    """)
    List<Long> findAllIdByPostId(Long postId);
}
