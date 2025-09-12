package com.knockbook.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
@Builder
public class GetProductsResponse {
    private List<ProductSummaryDTO> products;

    private String category;
    private String sort;
    // 선택적
    private String searchKeyword;
    private Integer minPrice;
    private Integer maxPrice;

    // pageResult
    private Integer page;
    private Integer size;
    private Long totalItems;
    private Integer totalPages;
    private boolean hasNext;
    private boolean hasPrevious;
}
