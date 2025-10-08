package com.knockbook.backend.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import java.util.List;
import java.util.Map;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class GetProductReviewsResponse {
    private List<ProductReviewDTO> productReviews;

    private Integer page;
    private Integer size;
    private Long totalItems;
    private Integer totalPages;

    private Double averageRating;
    private Map<Integer, Integer> starCounts;
}
