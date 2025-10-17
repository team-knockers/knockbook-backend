package com.knockbook.backend.repository;

import com.knockbook.backend.domain.FeedComment;
import com.knockbook.backend.domain.FeedProfileThumbnail;

import java.util.List;
import java.util.Optional;

public interface FeedWriteRepository {
    FeedComment insertComment (
            Long postId,
            Long userId,
            String commentBody
    );

    void incrementPostCommentsCount (
            Long postId
    );

    FeedProfileThumbnail insertPost (
            Long userId,
            String content,
            List<String> imageUrls
    );

    Optional<Long> findPostIdByCommentIdAndUserId (
            Long commentId,
            Long userId
    );

    long deleteCommentByIdAndUserId (
            Long commentId,
            Long userId
    );

    void decrementPostCommentsCount (
            Long postId
    );

    long deletePostByIdAndUserId (
            Long postId,
            Long userId
    );
}
