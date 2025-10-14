package com.knockbook.backend.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class GetFeedProfileResponse {
    private String userId;
    private String displayName;
    private String avatarUrl;
    private String bio;

    private Long postsCount;
    private String nextAfter;

    private List<FeedProfileThumbnailDTO> profileThumbnails;
}
