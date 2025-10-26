package com.knockbook.backend.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Product {

    public enum Status { ACTIVE, HIDDEN, DISCONTINUED }
    public enum Availability { AVAILABLE, OUT_OF_STOCK, PREORDER, BACKORDER, COMING_SOON, SOLD_OUT }

    private Long productId;
    private Long categoryId;
    private String sku;
    private String name;
    private Integer stockQty;
    private Integer unitPriceAmount;
    private Integer salePriceAmount;
    private String manufacturerName;
    private String isImported;
    private String importCountry;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant deletedAt;
    private Instant releasedAt;
    private Status status;
    private Availability availability;
    private BigDecimal averageRating;
    private Integer reviewCount;
}
