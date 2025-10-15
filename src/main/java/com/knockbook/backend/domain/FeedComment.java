package com.knockbook.backend.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class FeedComment {
    private String commentId;
    private String userId;
    private String displayName;
    private String avatarUrl;
    private String body;
    private Instant createdAt;

    private Boolean likedByMe;
    private Integer likesCount;
}
