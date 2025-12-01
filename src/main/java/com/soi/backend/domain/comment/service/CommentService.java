package com.soi.backend.domain.comment.service;

import com.soi.backend.domain.comment.dto.CommentReqDto;
import com.soi.backend.domain.comment.dto.CommentRespDto;
import com.soi.backend.domain.comment.entity.Comment;
import com.soi.backend.domain.comment.repository.CommentRepository;
import com.soi.backend.domain.media.service.MediaService;
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

import java.util.List;

@Service
@RequiredArgsConstructor

public class CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final PostRepository postRepository;
    private final MediaService mediaService;

    @Transactional
    public void addComment(CommentReqDto commentReqDto) {
        User user = userRepository.findById(commentReqDto.getUserId())
                .orElseThrow(() -> new CustomException("유저를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        Long commentId = createComment(commentReqDto);
        Post post = postRepository.findById(commentReqDto.getPostId())
                        .orElseThrow(() -> new CustomException("게시물을 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        if (!post.getUserId().equals(commentReqDto.getUserId())) {
            notificationService.sendPostCommentNotification(
                    commentReqDto.getUserId(),
                    post.getUserId(),
                    commentId,
                    post.getId(),
                    notificationService.makeMessage(user.getId(), post.getContent(), NotificationType.COMMENT_ADDED)
            );
        }
    }

    @Transactional
    public Long createComment(CommentReqDto commentReqDto) {
        Comment comment = new Comment(
                commentReqDto.getUserId(),
                commentReqDto.getEmojiId(),
                commentReqDto.getPostId(),
                commentReqDto.getText(),
                commentReqDto.getAudioKey(),
                commentReqDto.getWaveformData(),
                commentReqDto.getDuration(),
                commentReqDto.getLocationX(),
                commentReqDto.getLocationY(),
                commentReqDto.getCommentType()
        );

        commentRepository.save(comment);
        return comment.getId();
    }

    @Transactional
    public void deleteComment(Long postId) {
        List<Long> commentIds = commentRepository.findAllIdByPostId(postId);
        for (Long commentId : commentIds) {
            commentRepository.deleteById(commentId);
        }
    }

    public List<CommentRespDto> getComments(Long postId) {
        postRepository.findById(postId)
                .orElseThrow(() -> new CustomException("게시물을 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        List<Comment> comments = commentRepository.findAllByPostId(postId);
        return toDto(comments);
    }

    private List<CommentRespDto> toDto(List<Comment> comments) {

        return comments.stream()
                .map(comment -> {

                    User user = userRepository.findById(comment.getUserId())
                            .orElseThrow(() -> new CustomException("유저를 찾을 수 없습니다.",  HttpStatus.NOT_FOUND));

                    String userProfileUrl = user.getProfileImageKey().isEmpty()
                            ? ""
                            : mediaService.getPresignedUrlByKey(user.getProfileImageKey());
                    String audioUrl =  comment.getAudioKey() == null || comment.getAudioKey().isEmpty()
                            ? ""
                            : mediaService.getPresignedUrlByKey(comment.getAudioKey());
                    return new CommentRespDto(
                            userProfileUrl,
                            comment.getText(),
                            comment.getEmojiId(),
                            audioUrl,
                            comment.getWaveformData(),
                            comment.getDuration(),
                            comment.getLocationX(),
                            comment.getLocationY(),
                            comment.getCommentType()
                    );
                })
                .toList();
    }
}
