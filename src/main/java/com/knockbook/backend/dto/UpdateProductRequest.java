package com.knockbook.backend.dto;

import com.knockbook.backend.entity.ProductEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateProductRequest {
    private Integer unitPriceAmount;
    private Integer salePriceAmount;
    private Integer stockQty;

    private ProductEntity.Status status;
    private ProductEntity.Availability availability;
}
