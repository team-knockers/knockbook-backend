package com.knockbook.backend.repository;

import com.knockbook.backend.domain.FeedComment;
import com.knockbook.backend.domain.FeedProfileThumbnail;

import java.util.List;

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
}
