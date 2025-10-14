package com.knockbook.backend.repository;
import com.knockbook.backend.domain.FeedPostsResult;
import com.knockbook.backend.domain.FeedProfileResult;

public interface FeedRepository {
    FeedPostsResult findFeedPosts(
            Long userId,
            String searchKeyword,
            Long after,
            int size
    );

    FeedProfileResult findFeedProfile(
            Long userId,
            Long after,
            int size
    );
}
