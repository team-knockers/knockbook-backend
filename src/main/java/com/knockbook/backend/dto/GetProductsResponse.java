package com.knockbook.backend.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class GetProductsResponse {
    private List<ProductSummaryDTO> products;

    private Integer page;
    private Integer size;
    private Long totalItems;
    private Integer totalPages;
}
