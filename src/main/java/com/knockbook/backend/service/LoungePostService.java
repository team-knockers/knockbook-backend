package com.knockbook.backend.service;

import com.knockbook.backend.component.ImgbbUploader;
import com.knockbook.backend.domain.*;
import com.knockbook.backend.exception.CommentNotFoundException;
import com.knockbook.backend.exception.PostNotFoundException;
import com.knockbook.backend.repository.LoungePostCommentRepository;
import com.knockbook.backend.repository.LoungePostLikeRepository;
import com.knockbook.backend.repository.LoungePostRepository;
import jakarta.persistence.PersistenceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class LoungePostService {

    @Autowired
    private LoungePostRepository postRepo;

    @Autowired
    private LoungePostCommentRepository postCommentRepo;

    @Autowired
    private LoungePostLikeRepository postLikeRepo;

    @Autowired
    private UserService userService;

    @Autowired
    private ImgbbUploader imgbb;


    @Transactional
    public LoungePost createPost(LoungePost post, List<MultipartFile> images) {
        if (post == null) {
            throw new IllegalArgumentException("Post must not be null");
        }

        final var trimmedTitle = post.getTitle() == null ? null : post.getTitle().trim();
        final var trimmedSubtitle = post.getSubtitle() == null ? null : post.getSubtitle().trim();
        final var trimmedContent = post.getContent() == null ? null : post.getContent().trim();

        if (trimmedTitle == null || trimmedTitle.isEmpty()) {
            throw new IllegalArgumentException("Title must not be empty");
        }
        if (trimmedContent == null || trimmedContent.isEmpty()) {
            throw new IllegalArgumentException("Content must not be empty");
        }

        // 1. Upload images and replace blob URLs
        String processedContent = trimmedContent;

        if (images != null && !images.isEmpty()) {
            final var blobUrlPattern = Pattern.compile("blob:[a-zA-Z0-9\\-:/.]+");

            for (final MultipartFile file : images) {
                if (file == null || file.isEmpty()) {
                    continue;
                }

                // 1) Find the next blob URL
                final var matcher = blobUrlPattern.matcher(processedContent);
                if (!matcher.find()) {
                    break; // 더 이상 blob URL이 없으면 중단
                }


                // 2) Upload and replace URL
                final var uploadedUrl = imgbb.upload(file);
                processedContent = processedContent.replaceFirst(blobUrlPattern.pattern(), uploadedUrl);
            }
        }

        // 2. Extract the first image URL → previewImageUrl
        final var previewImageUrl = extractFirstImageUrl(processedContent);

        // 3. Save the post
        final var postToSave = post.toBuilder()
                .title(trimmedTitle)
                .subtitle(trimmedSubtitle)
                .content(processedContent) // ← 핵심 수정: 치환된 content 저장
                .previewImageUrl(previewImageUrl)
                .status(LoungePost.Status.VISIBLE)
                .likeCount(0)
                .build();

        try {
            return postRepo.save(postToSave);
        } catch (PersistenceException e) {
            throw new RuntimeException("Failed to save LoungePost", e);
        }
    }

    @Transactional
    public void hardDeletePost(Long postId, Long userId) {
        postRepo.deleteByIdAndUserId(postId, userId);
    }

    public Page<LoungePostSummary> getPostsSummary(Pageable pageable) {

        final var page = postRepo.findPostsByPageable(pageable);

        final var userIds = page.getContent().stream()
                .map(LoungePostSummary::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        final var displayNameMap = new HashMap<Long, String>();
        for (final var userId : userIds) {
            try {
                final var user = userService.getUser(userId);
                final var name = user.getDisplayName();
                displayNameMap.put(userId, name);
            } catch (final Exception e) {
                displayNameMap.put(userId, "Unknown");
            }
        }

        final var updatedContent = page.getContent().stream()
                .map(summary -> summary.toBuilder()
                        .displayName(displayNameMap.get(summary.getUserId()))
                        .build())
                .collect(Collectors.toList());

        return new PageImpl<>(updatedContent, pageable, page.getTotalElements());
    }

    public LoungePost getPostDetails(Long id) {
        final var post = postRepo.findById(id)
                .orElseThrow(() -> new PostNotFoundException(String.valueOf(id)));

        final var userId = post.getUserId();
        if (userId == null) {
            throw new IllegalStateException("Post has no userId, postId=" + id);
        }

        final var user = userService.getUser(userId);

        return post.toBuilder()
                .displayName(user.getDisplayName())
                .avatarUrl(user.getAvatarUrl())
                .bio(user.getBio())
                .build();
    }

    @Transactional
    public LoungePostComment createComment(Long postId, Long userId, String content) {
        final var user = userService.getUser(userId);

        final var newComment = LoungePostComment.builder()
                .postId(postId)
                .userId(userId)
                .content(content)
                .status(LoungePostComment.Status.VISIBLE)
                .build();

        final var saved = postCommentRepo.save(newComment);

        return saved.toBuilder()
                .displayName(user.getDisplayName())
                .avatarUrl(user.getAvatarUrl())
                .build();
    }

    public LoungePostComment getComment(Long id) {
        return postCommentRepo.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new CommentNotFoundException("댓글이 존재하지 않습니다."));
    }

    public Page<LoungePostComment> getCommentsByPostId(Long postId, Pageable pageable) {
        // 1) Retrieve paginated comments
        final var page = postCommentRepo.findAllByPostIdAndNotDeleted(postId, pageable);

        // 2) Extract user IDs from comments
        final var userIds = page.getContent().stream()
                .map(LoungePostComment::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // 3) Map userId to displayName and avatarUrl
        final var userMap = new HashMap<Long, User>();
        for (final var userId : userIds) {
            try {
                final var user = userService.getUser(userId);
                userMap.put(userId, user);
            } catch (final Exception e) {
                userMap.put(userId, User.builder()
                        .displayName("Unknown")
                        .avatarUrl(null)
                        .build());
            }
        }

        // 4) Inject displayName and avatarUrl into each comment
        final var updatedComments = page.getContent().stream()
                .map(comment -> {
                    final var user = userMap.get(comment.getUserId());
                    return comment.toBuilder()
                            .displayName(user.getDisplayName())
                            .avatarUrl(user.getAvatarUrl())
                            .build();
                })
                .collect(Collectors.toList());

        // 5) Return as Page object
        return new PageImpl<>(updatedComments, pageable, page.getTotalElements());
    }

    @Transactional
    public LoungePostComment updateComment(Long id, Long userId, String newContent) {
        final var user = userService.getUser(userId);

        final var updated = postCommentRepo.updateContentById(id, userId, newContent);

        return updated.toBuilder()
                .displayName(user.getDisplayName())
                .avatarUrl(user.getAvatarUrl())
                .build();
    }

    @Transactional
    public void deleteComment(Long id, Long userId) {
        postCommentRepo.softDeleteById(id, userId);
    }

    @Transactional
    public void likePost(Long userId, Long postId) {
        final var changed = postLikeRepo.savePostLike(userId, postId);
        if (changed) {
            postLikeRepo.incrementLikeCount(postId);
        }
    }

    @Transactional
    public void unlikePost(Long userId, Long postId) {
        final var deleted = postLikeRepo.deletePostLikeIfExists(userId, postId);
        if (deleted) {
            postLikeRepo.decrementLikeCount(postId);
        }
    }

    public boolean isPostLikedByUser(Long userId, Long postId) {
        return postLikeRepo.existsByUserIdAndPostIdAndIsLikedTrue(userId, postId);
    }

    // Helper: Extracts the first image URL from the given content
    private String extractFirstImageUrl(String content) {
        if (content == null) {
            return null;
        }
        final var pattern = Pattern.compile("https?://[^\\s)'\"]+\\.(png|jpg|jpeg|gif)");
        final var matcher = pattern.matcher(content);
        return matcher.find() ? matcher.group() : null;
    }
}
