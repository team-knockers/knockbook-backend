package com.knockbook.backend.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class ProductDetailDTO {
    private String name;
    private Integer unitPriceAmount;
    private Integer salePriceAmount;
    private String manufacturerName;
    private String isImported;
    private String importCountry;
    private BigDecimal averageRating;
    private Integer reviewCount;

    // GALLERY: 최대 4장, sort_order 1..4
    private List<String> galleryImageUrls;
    // DESCRIPTION: 본문 이미지 전부
    private List<String> descriptionImageUrls;
}