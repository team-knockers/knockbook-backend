package com.knockbook.backend.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class ProductReviewsResult {
    private List<ProductReview> productReviews;
    private ProductReviewStats stats;

    private int page;
    private int size;
    private int totalPages;
}
