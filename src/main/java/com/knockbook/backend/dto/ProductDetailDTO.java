package com.knockbook.backend.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
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
    private Double averageRating;
    private Integer reviewCount;

    private List<String> galleryImageUrls;
    private List<String> descriptionImageUrls;
}
