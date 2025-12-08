package com.soi.backend.domain.post.service;

import com.soi.backend.domain.category.entity.CategoryUser;
import com.soi.backend.domain.category.repository.CategoryRepository;
import com.soi.backend.domain.category.repository.CategoryUserRepository;
import com.soi.backend.domain.category.service.CategorySetService;
import com.soi.backend.domain.comment.service.CommentService;
import com.soi.backend.domain.friend.entity.Friend;
import com.soi.backend.domain.friend.entity.FriendStatus;
import com.soi.backend.domain.friend.repository.FriendRepository;
import com.soi.backend.domain.media.service.MediaService;
import com.soi.backend.domain.notification.entity.NotificationType;
import com.soi.backend.domain.notification.service.NotificationService;
import com.soi.backend.domain.post.dto.PostCreateReqDto;
import com.soi.backend.domain.post.dto.PostRespDto;
import com.soi.backend.domain.post.dto.PostUpdateReqDto;
import com.soi.backend.domain.post.entity.Post;
import com.soi.backend.domain.post.entity.PostStatus;
import com.soi.backend.domain.post.repository.PostRepository;
import com.soi.backend.domain.user.entity.User;
import com.soi.backend.domain.user.repository.UserRepository;
import com.soi.backend.global.exception.CustomException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor

public class PostService {

    private final MediaService mediaService;
    private final PostRepository postRepository;
    private final CategorySetService categorySetService;
    private final CategoryRepository categoryRepository;
    private final CategoryUserRepository categoryUserRepository;
    private final FriendRepository friendRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final CommentService commentService;

    @Transactional
    public Boolean addPostToCategory(PostCreateReqDto postCreateReqDto) {

        for (int i=0; i<postCreateReqDto.getCategoryId().size(); i++) {
            Long categoryId = postCreateReqDto.getCategoryId().get(i);
            String fileKey = postCreateReqDto.getPostFileKey().get(i);
            String audioFileKey = "";
            if (postCreateReqDto.getPostFileKey().size() == postCreateReqDto.getAudioFileKey().size()) {
                audioFileKey = postCreateReqDto.getPostFileKey().get(i);
            }

            createPost(postCreateReqDto, categoryId, fileKey, audioFileKey);

            List<Long> receivers =
                    categoryUserRepository.findAllUserIdsByCategoryIdExceptUser(categoryId, postCreateReqDto.getId());

            CategoryUser categoryUser = categoryUserRepository.findByCategoryIdAndUserId(categoryId, postCreateReqDto.getId())
                    .orElseThrow(() -> new CustomException("카테고리를 찾을 수 없음", HttpStatus.NOT_FOUND));

            categoryUser.setLastViewedAt();

            String categoryName = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new CustomException("카테고리를 찾을 수 없음",HttpStatus.NOT_FOUND))
                    .getName();

            categorySetService.setLastUploadedAndProfile(categoryId, postCreateReqDto.getId(), fileKey);

            for (Long receiverId : receivers) {
                notificationService.sendCategoryPostNotification(
                        postCreateReqDto.getId(),
                        receiverId,
                        categoryId,
                        notificationService.makeMessage(postCreateReqDto.getId(), categoryName, NotificationType.PHOTO_ADDED),
                        fileKey
                );
            }
        }
        return true;
    }

    @Transactional
    public void createPost(PostCreateReqDto postCreateReqDto, Long categoryId, String fileKey, String audioFileKey) {
        Post post = new Post(
                postCreateReqDto.getId(),
                postCreateReqDto.getContent(),
                fileKey,
                audioFileKey,
                categoryId,
                postCreateReqDto.getWaveformData(),
                postCreateReqDto.getDuration()
        );

        postRepository.save(post);
    }

    @Transactional
    public void updatePost(PostUpdateReqDto postUpdateReqDto) {
        Post originalPost = postRepository.findById(postUpdateReqDto.getPostId())
                .orElseThrow(() -> new CustomException(postUpdateReqDto.getPostId() + " id의 게시물을 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        originalPost.update(
                postUpdateReqDto.getContent(),
                postUpdateReqDto.getPostFileKey(),
                postUpdateReqDto.getAudioFileKey(),
                postUpdateReqDto.getWaveformData(),
                postUpdateReqDto.getDuration()
        );

        postRepository.save(originalPost);
    }

    @Transactional
    public PostStatus setPostStatus(Long postId, PostStatus postStatus) {
        Post originalPost = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(postId + " id의 게시물을 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        originalPost.setStatus(postStatus, postStatus ==  PostStatus.ACTIVE);

        postRepository.save(originalPost);

        return originalPost.getStatus();
    }

    @Transactional
    public void hardDelete(Post post) {
        // 게시물과 관련된 알림 삭제
        notificationService.deletePostNotification(post.getUserId(), post.getId());
        // 카테고리에 등록된 모든 post 삭제
        commentService.deleteComments(post.getId());

        // s3파일 삭제
        mediaService.removeMedia(post.getFileKey());
        mediaService.removeMedia(post.getAudioKey());

        postRepository.deleteById(post.getId());
    }

    @Transactional
    public void hardDeletePosts(Long categoryId) {
        // 카테고리에 등록된 모든 post를 찾음
        List<Post> posts = postRepository.findAllByCategoryId(categoryId);

        for (Post post : posts) {
            hardDelete(post);
        }
    }

    @Transactional
    public void hardDeletePost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException("게시물이 없습니다.", HttpStatus.NOT_FOUND));
        hardDelete(post);
    }

    public List<PostRespDto> findByCategoryId(Long categoryId, Long userId) {
        // 차단한 유저들 가져오기
        List<Friend> BlockedUsers = friendRepository.findAllFriendsByUserIdAndStatus(userId, FriendStatus.BLOCKED);
        Set<Long> BlockedUserIds = BlockedUsers.stream()
                .map(friend -> {
                    if (friend.getReceiverId().equals(userId)) {
                        return friend.getRequesterId();
                    } else  {
                        return friend.getReceiverId();
                    }
                }).collect(Collectors.toSet());

        categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CustomException("카테고리를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        // 카테고리에 있는 게시물 가져오기
        List<Post> posts = postRepository.findAllByCategoryIdAndStatusAndIsActiveOrderByCreatedAtDesc(categoryId, PostStatus.ACTIVE, true);

        // 차단된 유저가 올린 게시물을 필터링하기
        List<Post> filteredPosts = posts.stream()
                .filter(post -> !BlockedUserIds.contains(post.getUserId()))
                .collect(Collectors.toList());

        categorySetService.setLastViewed(categoryId, userId);

        return filteredPosts.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    // 게시물 출력하기
    public List<PostRespDto> findPostToShowMainPage(Long userId, PostStatus postStatus) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException("사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        List<Long> categoryIds = categoryUserRepository.findCategoriesByUserId(userId);

        List<Post> posts = new ArrayList<>(postRepository.findAllByCategoryIdInAndStatusAndIsActiveOrderByCreatedAtDesc(
                categoryIds,
                postStatus,
                postStatus == PostStatus.ACTIVE));

        return posts.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    // 단일 게시물 상세페이지 정보
    public PostRespDto showPostDetail(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException("게시물을 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        return toDto(post);
    }

    private PostRespDto toDto(Post post) {
        User user = userRepository.findById(post.getUserId())
                .orElseThrow(() -> new CustomException("유저를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        return new PostRespDto(
                post.getId(),
                user.getNickname(),
                post.getContent(),
                user.getProfileImageKey(),
                post.getFileKey(),
                post.getAudioKey(),
                post.getWaveformData(),
                post.getDuration(),
                post.getIsActive(),
                post.getCreatedAt()
        );
    }
}
