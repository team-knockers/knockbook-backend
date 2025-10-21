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
    private Long postsCount;
    private String nextAfter;

    private List<FeedProfileThumbnailDTO> profileThumbnails;
}
