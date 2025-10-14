package com.knockbook.backend.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class FeedPostDTO {
    private String postId;
    private String userId;
    private String displayName;
    private String avatarUrl;

    private String content;
    private List<String> images; // ordered URLs

    private Integer likesCount;
    private Integer commentsCount;
    private Boolean likedByMe;
    private String createdAtAgo; // "3시간 전"
}
