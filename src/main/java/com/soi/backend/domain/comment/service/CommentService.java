package com.soi.backend.domain.comment.service;

import com.soi.backend.domain.category.entity.CategoryUser;
import com.soi.backend.domain.category.repository.CategoryRepository;
import com.soi.backend.domain.category.repository.CategoryUserRepository;
import com.soi.backend.domain.comment.dto.CommentReqDto;
import com.soi.backend.domain.comment.dto.CommentRespDto;
import com.soi.backend.domain.comment.entity.Comment;
import com.soi.backend.domain.comment.entity.CommentType;
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

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor

public class CommentService {

    private final CommentRepository commentRepository;
    private final CategoryUserRepository categoryUserRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final PostRepository postRepository;
    private final MediaService mediaService;

    @Transactional
    public void addComment(CommentReqDto commentReqDto) {
        User user = userRepository.findById(commentReqDto.getUserId())
                .orElseThrow(() -> new CustomException("유저를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
        Post post = postRepository.findById(commentReqDto.getPostId())
                        .orElseThrow(() -> new CustomException("게시물을 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        Long commentId = createComment(commentReqDto);
        CommentType commentType = commentReqDto.getCommentType();
        NotificationType notificationType;
        Long categoryId = post.getCategoryId();

        switch (commentType) {
            case TEXT, PHOTO -> notificationType = NotificationType.COMMENT_ADDED;
            case AUDIO -> notificationType = NotificationType.COMMENT_AUDIO_ADDED;
            case EMOJI -> notificationType = NotificationType.COMMENT_REACT_ADDED;
            case REPLY ->  notificationType = NotificationType.COMMENT_REPLY_ADDED;
            default -> notificationType = null;
        }

        List<CategoryUser> categoryUsers =
                categoryUserRepository.findAllByCategoryIdExceptUser(categoryId, user.getId());

        if (commentType.equals(CommentType.REPLY)) {
            Comment parentComment = commentRepository.findById(commentId)
                    .orElseThrow(() -> new CustomException("댓글을 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
            Long receiverId = parentComment.getReplyUserId();
            notificationService.sendPostCommentNotification(
                    commentReqDto.getUserId(),
                    receiverId,
                    commentId,
                    post.getId(),
                    categoryId,
                    notificationService.makeMessage(user.getId(), post.getContent(), notificationType),
                    notificationType
            );
        } else {
            for (CategoryUser receivers : categoryUsers) {
                Long receiverId = receivers.getUserId();
                if (receivers.getIsAlert()) {
                    notificationService.sendPostCommentNotification(
                            commentReqDto.getUserId(),
                            receiverId,
                            commentId,
                            post.getId(),
                            categoryId,
                            notificationService.makeMessage(user.getId(), post.getContent(), notificationType),
                            notificationType
                    );
                }
            }
        }
    }

    @Transactional
    public Long createComment(CommentReqDto commentReqDto) {
        Comment comment = new Comment(
                commentReqDto.getUserId(),
                commentReqDto.getEmojiId(),
                commentReqDto.getPostId(),
                commentReqDto.getParentId(),
                commentReqDto.getReplyUserId(),
                commentReqDto.getText(),
                commentReqDto.getAudioKey(),
                commentReqDto.getFileKey(),
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
    public void deleteComment(Long commentId) {
        commentRepository.deleteById(commentId);
    }

    @Transactional
    public void deleteComments(Long postId) {
        List<Long> commentIds = commentRepository.findAllIdByPostId(postId);
        for (Long commentId : commentIds) {
            deleteComment(commentId);
        }
    }

    public List<CommentRespDto> getComments(Long postId) {
        postRepository.findById(postId)
                .orElseThrow(() -> new CustomException("게시물을 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        List<Comment> comments = commentRepository.findAllByPostId(postId);
        return toDto(comments);
    }

    private List<CommentRespDto> toDto(List<Comment> comments) {

        comments.sort(Comparator.comparing(Comment::getCreatedAt));

        Map<Long, List<Comment>> childMap = comments.stream()
                .filter(c -> c.getParentId() != null)
                .collect(Collectors.groupingBy(Comment::getParentId));

        Set<Long> userIds = comments.stream()
                .map(Comment::getUserId)
                .collect(Collectors.toSet());

        Map<Long, User> userMap = userRepository.findAllById(userIds)
                .stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        return comments.stream()
                .filter(c -> c.getParentId() == null)
                .map(parent -> buildParentDto(parent, childMap, userMap))
                .toList();
    }

    private CommentRespDto buildParentDto(Comment parent, Map<Long, List<Comment>> childMap, Map<Long, User> userMap) {
        List<CommentRespDto> children = childMap
                .getOrDefault(parent.getId(), List.of())
                .stream()
                .map(child -> buildChildDto(child, userMap))
                .toList();

        return buildBaseDto(parent, userMap.get(parent.getUserId()), children);
    }

    private CommentRespDto buildChildDto(Comment child, Map<Long, User> userMap) {
        return buildBaseDto(child, userMap.get(child.getUserId()), List.of());
    }

    private CommentRespDto buildBaseDto(Comment comment, User user, List<CommentRespDto> children) {

        String userProfileUrl = (user.getProfileImageKey() == null || user.getProfileImageKey().isEmpty())
                ? ""
                : mediaService.getPresignedUrlByKey(user.getProfileImageKey());

        String audioUrl = (comment.getAudioKey() == null || comment.getAudioKey().isEmpty())
                ? ""
                : mediaService.getPresignedUrlByKey(comment.getAudioKey());

        String fileUrl = (comment.getFileKey() == null || comment.getFileKey().isBlank())
                ? ""
                : mediaService.getPresignedUrlByKey(comment.getFileKey());

        String replyUserNickName = (comment.getReplyUserId() == null)
                ? null
                : userRepository.findById(comment.getReplyUserId()).get().getNickname();

        return new CommentRespDto(
                comment.getId(),
                userProfileUrl,
                user.getNickname(),
                comment.getText(),
                comment.getEmojiId(),
                replyUserNickName,
                audioUrl,
                comment.getWaveformData(),
                comment.getDuration(),
                comment.getLocationX(),
                comment.getLocationY(),
                comment.getCommentType(),
                fileUrl,
                comment.getFileKey(),
                children
        );
    }
}
