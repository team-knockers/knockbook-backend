package com.knockbook.backend.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(toBuilder = true)
public class RandomBookReview {
    private Long id;
    private Long userId;
    private String displayName;
    private Long bookId;
    private String coverThumbnailUrl;
    private String content;
}
