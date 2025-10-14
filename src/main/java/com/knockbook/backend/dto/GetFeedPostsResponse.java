package com.knockbook.backend.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

// Home feed page response (keyset pagination with ?after=postId)
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class GetFeedPostsResponse {
    private List<FeedPostDTO> feedPosts;
    private String nextAfter; // null = no more
}
