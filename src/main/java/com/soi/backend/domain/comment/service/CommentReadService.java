package com.soi.backend.domain.comment.service;

import com.soi.backend.domain.comment.entity.Comment;
import com.soi.backend.domain.comment.repository.CommentRepository;
import com.soi.backend.global.exception.CustomException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor

public class CommentReadService {

    private final CommentRepository commentRepository;

    @Transactional
    public Long getParentCommentIdOfReply(Long replyCommentId) {
        Comment reply = commentRepository.findById(replyCommentId)
                .orElseThrow(() -> new CustomException("댓글을 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        if (reply.getParentId() == null || reply.getParentId() == 0L) {
            throw new CustomException("대댓글이 아닙니다.", HttpStatus.BAD_REQUEST);
        }

        return reply.getParentId();
    }
}
