package com.knockbook.backend.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class ProductSummary {

    public enum Status { ACTIVE, HIDDEN, DISCONTINUED }
    public enum Availability { AVAILABLE, OUT_OF_STOCK, PREORDER, BACKORDER, COMING_SOON, SOLD_OUT }

    private Long id;
    private Long categoryId;
    private String sku;
    private String name;
    private Integer unitPriceAmount;
    private Integer salePriceAmount;
    private Integer stockQty;
    private Status status;
    private Availability availability;

    private BigDecimal averageRating;
    private Integer reviewCount;
    private String thumbnailUrl;
}
