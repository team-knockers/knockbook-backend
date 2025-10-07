package com.knockbook.backend.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class ProductReviewStats {
    private double averageRating;
    private Map<Integer, Integer> starCounts;
    private Long totalItems;
}
