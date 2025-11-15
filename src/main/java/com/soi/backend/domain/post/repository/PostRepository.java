package com.soi.backend.domain.post.repository;

import com.soi.backend.domain.post.entity.Post;
import com.soi.backend.domain.post.entity.PostStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findAllByCategoryIdAndStatusAndIsActiveOrderByCreatedAtDesc(Long categoryId,
                                                                           PostStatus status,
                                                                           Boolean isActive);
    List<Post> findAllByCategoryIdInAndStatusAndIsActiveOrderByCreatedAtDesc(List<Long> categoryId,
                                                                           PostStatus status,
                                                                           Boolean isActive);
}
