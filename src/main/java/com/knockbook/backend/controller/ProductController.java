package com.knockbook.backend.controller;

import com.knockbook.backend.domain.ProductSummary;
import com.knockbook.backend.dto.GetProductsResponse;
import com.knockbook.backend.dto.ProductSummaryDTO;
import com.knockbook.backend.service.ProductReadService;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
@Validated
public class ProductController {
    private final ProductReadService productReadService;

    @GetMapping("")
    public ResponseEntity<GetProductsResponse> getProducts(
            @RequestParam String category,
            @RequestParam String sort,
            @RequestParam(required = false) String searchKeyword,
            @RequestParam(required = false) Integer minPrice,
            @RequestParam(required = false) Integer maxPrice,
            @RequestParam @Min(1)int page,
            @RequestParam @Min(1) int size
            ){
        // 서비스 호출
        Page<ProductSummary> result = productReadService.getProducts(
                category, sort, searchKeyword, minPrice, maxPrice, page, size
        );

        List<ProductSummaryDTO> items = result.getContent().stream().map(s -> ProductSummaryDTO.builder()
                .name(s.getName())
                .unitPriceAmount(s.getUnitPriceAmount())
                .salePriceAmount(s.getSalePriceAmount())
                .averageRating(s.getAverageRating())
                .reviewCount(s.getReviewCount())
                .thumbnailUrl(s.getThumbnailUrl())
                .availability(s.getAvailability() == null ? null : s.getAvailability().name())
                .build()
        ).toList();

        GetProductsResponse body = GetProductsResponse.builder()
                .products(items)
                .category(category)
                .sort(sort)
                .searchKeyword(searchKeyword)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .page(result.getNumber() + 1)
                .size(result.getSize())
                .totalItems(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .hasNext(result.hasNext())
                .hasPrevious(result.hasPrevious())
                .build();

        return ResponseEntity.ok(body);

    }
}
