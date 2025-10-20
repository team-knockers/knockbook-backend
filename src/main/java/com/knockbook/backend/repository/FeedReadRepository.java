package com.knockbook.backend.repository;
import com.knockbook.backend.domain.FeedCommentsResult;
import com.knockbook.backend.domain.FeedPostsResult;
import com.knockbook.backend.domain.FeedProfileResult;
import com.knockbook.backend.domain.FeedResult;

public interface FeedReadRepository {
    FeedPostsResult findFeedPosts(
            Long userId,
            String searchKeyword,
            Long after,
            int size,
            String mbti
    );

    FeedProfileResult findFeedProfile(
            Long userId,
            Long after,
            int size
    );

    FeedCommentsResult findFeedPostComments(
            Long userId,
            Long postId
    );

    FeedResult findFeedPostWithComments(
            Long userId,
            Long PostId
    );
}
