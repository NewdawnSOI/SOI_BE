package com.soi.backend.domain.post.service;

import com.soi.backend.domain.category.entity.CategoryUser;
import com.soi.backend.domain.category.repository.CategoryRepository;
import com.soi.backend.domain.category.repository.CategoryUserRepository;
import com.soi.backend.domain.category.service.CategoryService;
import com.soi.backend.domain.category.service.CategorySetService;
import com.soi.backend.domain.comment.service.CommentService;
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
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor

public class PostService {

    private final MediaService mediaService;
    private final PostRepository postRepository;
    private final CategorySetService categorySetService;
    private final CategoryRepository categoryRepository;
    private final CategoryUserRepository categoryUserRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final CommentService commentService;

    @Transactional
    public Boolean addPostToCategory(PostCreateReqDto postCreateReqDto) {

        for (Long categoryId : postCreateReqDto.getCategoryId()) {
            createPost(postCreateReqDto, categoryId);

            List<Long> receivers =
                    categoryUserRepository.findAllUserIdsByCategoryIdExceptUser(categoryId, postCreateReqDto.getId());

            String categoryName = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new CustomException("카테고리를 찾을 수 없음",HttpStatus.NOT_FOUND))
                    .getName();

            categorySetService.setLastUploaded(categoryId, postCreateReqDto.getId());

            for (Long receiverId : receivers) {
                notificationService.sendCategoryPostNotification(
                        postCreateReqDto.getId(),
                        receiverId,
                        categoryId,
                        notificationService.makeMessage(postCreateReqDto.getId(), categoryName, NotificationType.PHOTO_ADDED),
                        postCreateReqDto.getPostFileKey()
                );
            }
        }
        return true;
    }

    @Transactional
    public void createPost(PostCreateReqDto postCreateReqDto, Long categoryId) {
        Post post = new Post(
                postCreateReqDto.getId(),
                postCreateReqDto.getContent(),
                postCreateReqDto.getPostFileKey(),
                postCreateReqDto.getAudioFileKey(),
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
    public void softDeletePost(Long postId) {
        Post originalPost = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(postId + " id의 게시물을 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        originalPost.setStatus(PostStatus.DELETED, false);
        postRepository.save(originalPost);
    }

    @Transactional
    public void hardDeletePost(Long postId) {
        postRepository.findById(postId);
    }

    @Transactional
    public void hardDeletePosts(Long categoryId) {
        // 카테고리에 등록된 모든 post를 찾음
        List<Post> posts = postRepository.findAllByCategoryId(categoryId);

        for (Post post : posts) {
            // 게시물과 관련된 알림 삭제
            notificationService.deletePostNotification(post.getUserId(), post.getId());
            // 카테고리에 등록된 모든 post 삭제
            commentService.deleteComment(post.getId());

            // s3파일 삭제
            mediaService.removeMedia(post.getFileKey());
            mediaService.removeMedia(post.getAudioKey());

            postRepository.deleteById(post.getId());
        }
    }

    public List<PostRespDto> findByCategoryId(Long categoryId, Long userId) {
        categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CustomException("카테고리를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        List<Post> posts = postRepository.findAllByCategoryIdAndStatusAndIsActiveOrderByCreatedAtDesc(categoryId, PostStatus.ACTIVE, true);
        categorySetService.setLastViewed(categoryId, userId);

        return posts.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    // 메인페이지에 나올 전체 게시물 출력하기
    public List<PostRespDto> findPostToShowMainPage(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException("사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        List<Long> categoryIds = categoryUserRepository.findCategoriesByUserId(userId);

        List<Post> posts = new ArrayList<>(postRepository.findAllByCategoryIdInAndStatusAndIsActiveOrderByCreatedAtDesc(
                categoryIds,
                PostStatus.ACTIVE,
                true));

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
                user.getUserId(),
                post.getContent(),
                post.getFileKey().isEmpty() ? "" : mediaService.getPresignedUrlByKey(post.getFileKey()),
                post.getAudioKey().isEmpty() ? "" : mediaService.getPresignedUrlByKey(post.getAudioKey()),
                post.getWaveformData(),
                post.getDuration(),
                post.getIsActive(),
                post.getCreatedAt()
        );
    }
}
