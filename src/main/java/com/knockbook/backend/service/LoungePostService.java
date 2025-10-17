package com.knockbook.backend.service;

import com.knockbook.backend.domain.*;
import com.knockbook.backend.exception.CommentNotFoundException;
import com.knockbook.backend.exception.PostNotFoundException;
import com.knockbook.backend.repository.LoungePostCommentRepository;
import com.knockbook.backend.repository.LoungePostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class LoungePostService {

    @Autowired
    private LoungePostRepository postRepo;

    @Autowired
    private LoungePostCommentRepository postCommentRepo;

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

        /* TODO. UserService에서 bio정보 전달되면 변경할 것 */
        return post.toBuilder()
                .displayName(user.getDisplayName())
                .avatarUrl(user.getAvatarUrl())
//                .bio(user.getBio())
                .bio("임시 테스트 bio입니다.")
                .build();
    }

    @Transactional
    public LoungePostComment createComment(Long postId, Long userId, String content) {
        final var newComment = LoungePostComment.builder()
                .postId(postId)
                .userId(userId)
                .content(content)
                .status(LoungePostComment.Status.VISIBLE)
                .build();

        return postCommentRepo.save(newComment);
    }

    public LoungePostComment getComment(Long id) {
        return postCommentRepo.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new CommentNotFoundException("댓글이 존재하지 않습니다."));
    }

    public List<LoungePostComment> getCommentsByPostId(Long postId, Pageable pageable) {
        return postCommentRepo.findAllByPostIdAndNotDeleted(postId, pageable);
    }

    @Transactional
    public LoungePostComment updateComment(Long id, Long userId, String newContent) {
        return postCommentRepo.updateContentById(id, userId, newContent);
    }

    @Transactional
    public LoungePostComment deleteComment(Long id, Long userId) {
        return postCommentRepo.softDeleteById(id, userId);
    }
}
