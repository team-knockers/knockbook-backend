package com.knockbook.backend.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class FeedProfileResult {
    private Long postsCount;
    private String nextAfter;

    private List<FeedProfileThumbnail> profileThumbnails;
}
