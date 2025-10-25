package com.knockbook.backend.domain;

import com.knockbook.backend.entity.ProductEntity;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class ProductCreateSpec {
    private String categoryCode;
    private String sku;
    private String name;
    private Integer stockQty;
    private Integer unitPriceAmount;
    private Integer salePriceAmount;
    private String manufacturerName;
    private String isImported;
    private String importCountry;
    private Instant releasedAt;
    private ProductEntity.Status status;
    private ProductEntity.Availability availability;
    private List<String> galleryImageUrls;
    private List<String> descriptionImageUrls;
}
