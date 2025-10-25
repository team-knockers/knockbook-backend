package com.knockbook.backend.domain;

import com.knockbook.backend.entity.ProductEntity;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class ProductUpdateSpec {

    private Integer unitPriceAmount;
    private Integer salePriceAmount;
    private Integer stockQty;

    private ProductEntity.Status status;
    private ProductEntity.Availability availability;
}
