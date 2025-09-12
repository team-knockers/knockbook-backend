package com.knockbook.backend.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class ProductSummaryDTO {
    private String name;
    private Integer unitPriceAmount;
    private Integer salePriceAmount;
    private BigDecimal averageRating;
    private Integer reviewCount;
    private String thumbnailUrl;
    private String availability;
}
