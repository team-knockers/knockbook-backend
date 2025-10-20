package com.knockbook.backend.service;

import com.knockbook.backend.domain.*;
import com.knockbook.backend.exception.CommentNotFoundException;
import com.knockbook.backend.exception.PostNotFoundException;
import com.knockbook.backend.repository.LoungePostCommentRepository;
import com.knockbook.backend.repository.LoungePostLikeRepository;
import com.knockbook.backend.repository.LoungePostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Objects;
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
}
