package com.knockbook.backend.repository;

import com.knockbook.backend.domain.FeedComment;

public interface FeedWriteRepository {
    FeedComment insertComment (
            Long postId,
            Long userId,
            String commentBody
    );

    void incrementPostCommentsCount (
            Long postId
    );
}
