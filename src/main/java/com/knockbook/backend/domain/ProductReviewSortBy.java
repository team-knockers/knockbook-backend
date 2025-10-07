package com.knockbook.backend.domain;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public enum ProductReviewSortBy {
    createdAt, rating, likesCount;

    private static final Map<String, ProductReviewSortBy> LOOKUP =
            Arrays.stream(values())
                    .collect(Collectors.toUnmodifiableMap(Enum::name, e -> e));

    public static ProductReviewSortBy parseOrDefault(String s) {
        return LOOKUP.getOrDefault(s, createdAt);
    }
}
