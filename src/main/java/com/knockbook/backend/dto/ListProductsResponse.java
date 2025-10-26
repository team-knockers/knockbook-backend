package com.knockbook.backend.dto;

import com.knockbook.backend.domain.Product;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class ListProductsResponse {
    private String productId;
    private String categoryId;
    private String sku;
    private String name;
    private Integer stockQty;
    private Integer unitPriceAmount;
    private Integer salePriceAmount;
    private String manufacturerName;
    private String isImported;
    private String importCountry;
    private String createdAt;
    private String updatedAt;
    private String deletedAt;
    private String releasedAt;
    private String status;
    private String availability;
    private double averageRating;
    private Integer reviewCount;

    public static ListProductsResponse fromDomain(Product product) {
        return ListProductsResponse.builder()
                .productId(String.valueOf(product.getProductId()))
                .categoryId(String.valueOf(product.getCategoryId()))
                .sku(product.getSku())
                .name(product.getName())
                .stockQty(product.getStockQty())
                .unitPriceAmount(product.getUnitPriceAmount())
                .salePriceAmount(product.getSalePriceAmount())
                .manufacturerName(product.getManufacturerName())
                .isImported(product.getIsImported())
                .importCountry(product.getImportCountry())
                .createdAt(product.getCreatedAt() != null ? product.getCreatedAt().toString() : null)
                .updatedAt(product.getUpdatedAt() != null ? product.getUpdatedAt().toString() : null)
                .deletedAt(product.getDeletedAt() != null ? product.getDeletedAt().toString() : null)
                .releasedAt(product.getReleasedAt() != null ? product.getReleasedAt().toString() : null)
                .status(product.getStatus() != null ? product.getStatus().name() : null)
                .availability(product.getAvailability() != null ? product.getAvailability().name() : null)
                .averageRating(product.getAverageRating() != null ? product.getAverageRating().doubleValue() : 0.0)
                .reviewCount(product.getReviewCount())
                .build();
    }
}
