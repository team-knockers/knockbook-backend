package com.knockbook.backend.repository;
import com.knockbook.backend.domain.FeedPostsResult;

public interface FeedRepository {
    FeedPostsResult findFeedPosts(
            Long userId,
            String searchKeyword,
            Long after,
            int size
    );
}
