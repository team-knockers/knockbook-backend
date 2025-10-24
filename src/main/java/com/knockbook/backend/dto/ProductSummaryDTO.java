package com.knockbook.backend.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class ProductSummaryDTO {
    private String productId;
    private String name;
    private Integer unitPriceAmount;
    private Integer salePriceAmount;
    private Double averageRating;
    private Integer reviewCount;
    private String thumbnailUrl;
    private String availability;
    private Boolean wishedByMe;
}
