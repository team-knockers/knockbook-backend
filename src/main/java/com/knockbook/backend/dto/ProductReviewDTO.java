package com.knockbook.backend.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class ProductReviewDTO {
    private String reviewId;
    private String displayName;
    private String body;
    private Integer rating;
    private String createdAt;
    private Integer likesCount;
    private boolean likedByMe;
}
