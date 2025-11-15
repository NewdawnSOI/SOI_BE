package com.soi.backend.domain.post.service;

import com.soi.backend.domain.category.entity.Category;
import com.soi.backend.domain.category.repository.CategoryRepository;
import com.soi.backend.domain.category.repository.CategoryUserRepository;
import com.soi.backend.domain.media.service.MediaService;
import com.soi.backend.domain.post.dto.PostCreateReqDto;
import com.soi.backend.domain.post.dto.PostRespDto;
import com.soi.backend.domain.post.dto.PostUpdateReqDto;
import com.soi.backend.domain.post.entity.Post;
import com.soi.backend.domain.post.entity.PostStatus;
import com.soi.backend.domain.post.repository.PostRepository;
import com.soi.backend.domain.user.entity.User;
import com.soi.backend.domain.user.repository.UserRepository;
import com.soi.backend.global.exception.CustomException;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor

public class PostService {

    private final MediaService mediaService;
    private final PostRepository postRepository;
    private final CategoryRepository categoryRepository;
    private final CategoryUserRepository categoryUserRepository;
    private final UserRepository userRepository;

    @Transactional
    public Boolean addPostToCategory(PostCreateReqDto postCreateReqDto) {

        for(Long categoryId : postCreateReqDto.getCategoryId()) {
            createPost(postCreateReqDto, categoryId);
        }

        return true;
    }

    @Transactional
    public void createPost(PostCreateReqDto postCreateReqDto, Long categoryId) {
        Post post = new Post(
                postCreateReqDto.getUserId(),
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

    public List<PostRespDto> findByCategoryId(Long categoryId) {
        categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CustomException("카테고리를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        List<Post> posts = postRepository.findAllByCategoryIdAndStatusAndIsActiveOrderByCreatedAtDesc(categoryId, PostStatus.ACTIVE, true);

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
        return new PostRespDto(
                post.getUserId(),
                post.getContent(),
                post.getFileUrl().isEmpty() ? "" : mediaService.getPresignedUrlByKey(post.getFileUrl()),
                post.getAudioUrl().isEmpty() ? "" : mediaService.getPresignedUrlByKey(post.getAudioUrl()),
                post.getWaveformData(),
                post.getDuration(),
                post.getIsActive(),
                post.getCreatedAt()
        );
    }
}
