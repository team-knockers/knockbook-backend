package com.knockbook.backend.service;

import com.knockbook.backend.domain.FeedCommentsResult;
import com.knockbook.backend.domain.FeedPostsResult;
import com.knockbook.backend.domain.FeedProfileResult;
import com.knockbook.backend.domain.FeedResult;
import com.knockbook.backend.repository.FeedReadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FeedService {
    private final FeedReadRepository feedReadRepository;

    public FeedPostsResult getFeedPosts(
            Long userId,
            String searchKeyword,
            Long after,
            int size
    ) {
        return feedReadRepository.findFeedPosts(userId, searchKeyword, after, size);
    }

    public FeedProfileResult getFeedProfile(
            Long userId,
            Long after,
            int size
    ) {
        return feedReadRepository.findFeedProfile(userId, after, size);
    }

    public FeedCommentsResult getFeedPostComments(
            Long userId,
            Long postId
    ) {
        return feedReadRepository.findFeedPostComments(userId, postId);
    }

    public FeedResult getFeedPostWithComments(
            Long userId,
            Long postId
    ) {
        return feedReadRepository.findFeedPostWithComments(userId, postId);
    }
}
