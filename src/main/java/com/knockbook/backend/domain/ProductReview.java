package com.knockbook.backend.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class ProductReview {
    private Long reviewId;
    private String displayName;
    private String body;
    private int rating;
    private Instant createdAt;
    private int likesCount;
    private boolean likedByMe;
}
