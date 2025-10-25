package com.knockbook.backend.dto;

import com.knockbook.backend.entity.ProductEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateProductRequest {
    @NotBlank
    private String categoryCode;

    @NotBlank
    private String sku;

    @NotBlank
    private String name;

    @NotNull
    private Integer stockQty;

    @NotNull
    private Integer unitPriceAmount;

    // nullable 가능
    private Integer salePriceAmount;

    @NotBlank
    private String manufacturerName;

    @NotBlank
    private String isImported;
    @NotBlank
    private String importCountry;

    // optional
    private Instant releasedAt;

    @NotNull
    private ProductEntity.Status status;  // ACTIVE / HIDDEN / DISCONTINUED

    @NotNull
    private ProductEntity.Availability availability;

    @NotNull
    private List<String> galleryImageUrls;      // sort_order = 1..N, usage=GALLERY
    @NotNull
    private List<String> descriptionImageUrls;
}
