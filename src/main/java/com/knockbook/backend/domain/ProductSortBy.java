package com.knockbook.backend.domain;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public enum ProductSortBy {
    createdAt, unitPriceAmount, averageRating, reviewCount;

    private static final Map<String, ProductSortBy> LOOKUP =
            Arrays.stream(values())
                    .collect(Collectors.toUnmodifiableMap(Enum::name, e -> e));

    public static ProductSortBy parseOrDefault(String s) {
        return LOOKUP.getOrDefault(s, createdAt);
    }
}
