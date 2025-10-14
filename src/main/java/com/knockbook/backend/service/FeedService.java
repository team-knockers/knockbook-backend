package com.knockbook.backend.service;

import com.knockbook.backend.domain.FeedPostsResult;
import com.knockbook.backend.repository.FeedRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FeedService {
    private final FeedRepository feedRepository;

    public FeedPostsResult getFeedPosts(
            Long userId,
            String searchKeyword,
            Long after,
            int size
    ) {
        return feedRepository.findFeedPosts(userId, searchKeyword, after, size);
    }
}
