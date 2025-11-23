package com.soi.backend.domain.comment.service;

import com.soi.backend.domain.comment.dto.CommentReqDto;
import com.soi.backend.domain.comment.entity.Comment;
import com.soi.backend.domain.comment.repository.CommentRepository;
import com.soi.backend.domain.notification.entity.NotificationType;
import com.soi.backend.domain.notification.service.NotificationService;
import com.soi.backend.domain.post.entity.Post;
import com.soi.backend.domain.post.repository.PostRepository;
import com.soi.backend.domain.user.entity.User;
import com.soi.backend.domain.user.repository.UserRepository;
import com.soi.backend.global.exception.CustomException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor

public class CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final PostRepository postRepository;

    @Transactional
    public void addComment(CommentReqDto commentReqDto) {
        User user = userRepository.findById(commentReqDto.getUserId())
                .orElseThrow(() -> new CustomException("유저를 찾을 수 없음", HttpStatus.NOT_FOUND));

        Long commentId = createComment(commentReqDto);
        Post post = postRepository.findById(commentReqDto.getPostId())
                        .orElseThrow(() -> new CustomException("게시물을 찾을 수 없음", HttpStatus.NOT_FOUND));

        notificationService.sendPostCommentNotification(
                commentReqDto.getUserId(),
                post.getId(),
                commentId,
                post.getId(),
                notificationService.makeMessage(user.getId(), post.getContent(), NotificationType.COMMENT_ADDED)
        );
    }

    @Transactional
    public Long createComment(CommentReqDto commentReqDto) {
        Comment comment = new Comment(
                commentReqDto.getUserId(),
                commentReqDto.getEmojiId(),
                commentReqDto.getPostId(),
                commentReqDto.getText(),
                commentReqDto.getAudioUrl(),
                commentReqDto.getWaveformData(),
                commentReqDto.getDuration(),
                commentReqDto.getLocationX(),
                commentReqDto.getLocationY(),
                commentReqDto.getCommentType()
        );

        commentRepository.save(comment);
        return comment.getId();
    }
}
