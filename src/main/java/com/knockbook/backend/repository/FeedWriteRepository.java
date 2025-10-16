package com.knockbook.backend.repository;

public interface FeedWriteRepository {
    Long insertComment (
            Long postId,
            Long userId,
            String commentBody
    );

    void incrementPostCommentsCount (
            Long postId
    );
}
