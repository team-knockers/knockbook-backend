package com.knockbook.backend.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class FeedPost {
    private String postId;
    private String userId;
    private String displayName;
    private String avatarUrl;
    private String content;
    private List<String> images;     // URL list (sort_order asc)
    private Integer likesCount;
    private Integer commentsCount;
    private Boolean likedByMe;
    private Instant createdAt;
}
