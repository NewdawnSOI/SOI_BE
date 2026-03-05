package com.soi.backend.domain.comment.service;

import com.soi.backend.domain.category.entity.CategoryUser;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    public Slice<CommentRespDto> getParentComments(Long postId, int page) {
        postRepository.findById(postId)
                .orElseThrow(() -> new CustomException("게시물을 찾을 수 없습니다."));
        Pageable pageable = PageRequest.of(page, 5);

        Slice<Comment> parentComments =
                commentRepository.findParentCommentsByPostId(postId, pageable);

        Set<Long> userIds = parentComments.stream()
                .map(Comment::getUserId)
                .collect(Collectors.toSet());

        Map<Long, User> userMap = userRepository.findAllById(userIds)
                .stream()
                .collect(Collectors.toMap(User::getId, user -> user));

        return parentComments.map(comment ->
                buildBaseDto(comment, userMap.get(comment.getUserId()), userMap));
    }

    public Slice<CommentRespDto> getChildComments(Long parentCommentId, int page) {
        Pageable pageable = PageRequest.of(page, 5);

        Slice<Comment> childComments =
                commentRepository.findChildCommentsByParentId(parentCommentId, pageable);

        Set<Long> userIds = childComments.stream()
                .flatMap(c -> {
                    if (c.getReplyUserId() != null) {
                        return Stream.of(c.getUserId(), c.getReplyUserId());
                    }
                    return Stream.of(c.getUserId());
                })
                .collect(Collectors.toSet());

        Map<Long, User> userMap = userRepository.findAllById(userIds)
                .stream()
                .collect(Collectors.toMap(User::getId, user -> user));

        return childComments.map(comment ->
                buildBaseDto(comment, userMap.get(comment.getUserId()), userMap)
        );
    }

    public Slice<CommentRespDto> getAllCommentByUserId(Long userId, int page) {
        Pageable pageable = PageRequest.of(page, 6);

        Slice<Comment> comments = commentRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);

        Set<Long> userIds = comments.getContent().stream()
                .flatMap(c -> c.getReplyUserId() != null
                        ? Stream.of(c.getUserId(), c.getReplyUserId())
                        : Stream.of(c.getUserId()))
                .collect(Collectors.toSet());

        Map<Long, User> userMap = userRepository.findAllById(userIds)
                .stream()
                .collect(Collectors.toMap(User::getId, user -> user));

        return comments.map(comment ->
                buildBaseDto(comment, userMap.get(comment.getUserId()), userMap));
    }

    private CommentRespDto buildBaseDto(Comment comment, User user, Map<Long, User> userMap) {

        String userProfileUrl = (user.getProfileImageKey() == null || user.getProfileImageKey().isEmpty())
                ? ""
                : mediaService.getPresignedUrlByKey(user.getProfileImageKey());

        String audioUrl = (comment.getAudioKey() == null || comment.getAudioKey().isEmpty())
                ? ""
                : mediaService.getPresignedUrlByKey(comment.getAudioKey());

        String fileUrl = (comment.getFileKey() == null || comment.getFileKey().isBlank())
                ? ""
                : mediaService.getPresignedUrlByKey(comment.getFileKey());

        String replyUserNickName = null;
        Long replyUserCount = 0L;

        if (comment.getReplyUserId() != null && comment.getReplyUserId() != 0) {
            User replyUser = userMap.get(comment.getReplyUserId());
            replyUserNickName = replyUser.getNickname();
        } else {
            // comment중에 parent_id가 해당 comment의 id인경우를 카운트해야함
            replyUserCount = commentRepository.countByParentId(comment.getId());
        }

        return new CommentRespDto(
                comment.getId(),
                userProfileUrl,
                user.getProfileImageKey(),
                user.getId(),
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
                comment.getCreatedAt(),
                replyUserCount
        );
    }

    public Map<Long, Integer> getCommentCountsForPostIds(List<Long> postIds) {
        List<Object[]> results = commentRepository.countCommentByPostIds(postIds);

        return results.stream()
                        .collect(Collectors.toMap(
                                row -> (Long) row[0],
                                row -> ((Long) row[1]).intValue()
                        ));
    }

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
