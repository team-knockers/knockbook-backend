package com.knockbook.backend.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class FeedCommentDTO {
    private String commentId;
    private String userId;
    private String displayName;
    private String avatarUrl;
    private String body;
    private String createdAt;

    private Boolean likedByMe;
    private Integer likesCount;
}
