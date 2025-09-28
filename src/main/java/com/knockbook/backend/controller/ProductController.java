package com.knockbook.backend.controller;

import com.knockbook.backend.domain.ProductSortBy;
import com.knockbook.backend.domain.SortOrder;
import com.knockbook.backend.dto.GetProductsResponse;
import com.knockbook.backend.dto.ProductDetailDTO;
import com.knockbook.backend.dto.ProductSummaryDTO;
import com.knockbook.backend.service.ProductService;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
@Validated
public class ProductController {
    private final ProductService productReadService;

    @PreAuthorize("#userId == authentication.name")
    @GetMapping("/{userId}")
    public ResponseEntity<GetProductsResponse> getProducts(
            @PathVariable("userId") String userId,
            @RequestParam String category,
            @RequestParam(required = false) String searchKeyword,
            @RequestParam(required = false) Integer minPrice,
            @RequestParam(required = false) Integer maxPrice,
            @RequestParam @Min(1) int page,
            @RequestParam @Min(1) int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String order
            ) {
        // Step 1: Normalize paging inputs
        final var safePage = Math.max(1, page) - 1;
        final var safeSize = Math.max(1, size);

        // Step 2: Parse sorting (string -> enum)
        final var sortKeyEnum = ProductSortBy.parseOrDefault(sortBy);
        final var sortOrderEnum = SortOrder.parseOrDefault(order);

        //Step 3 : Build Sort & pageable
        final var sortDirection = (sortOrderEnum == SortOrder.asc) ? Sort.Direction.ASC : Sort.Direction.DESC;
        final var sortSpec      = Sort.by(new Sort.Order(sortDirection, sortKeyEnum.name()));
        final var pageable      = PageRequest.of(safePage, safeSize, sortSpec);

        // Step 4: Call the service
        final var result = productReadService.getProductList(
                category, searchKeyword, minPrice, maxPrice, pageable
        );

        // Step 4: Map domain -> response DTO
        final var products = result.getContent().stream().map(s -> ProductSummaryDTO.builder()
                .productId(String.valueOf(s.getId()))
                .name(s.getName())
                .unitPriceAmount(s.getUnitPriceAmount())
                .salePriceAmount(s.getSalePriceAmount())
                .averageRating(s.getAverageRating())
                .reviewCount(s.getReviewCount())
                .thumbnailUrl(s.getThumbnailUrl())
                .availability(s.getAvailability() == null ? null : s.getAvailability().name())
                .build()
        ).toList();

        final var body = GetProductsResponse.builder()
                .products(products)
                .page(result.getNumber() + 1)
                .size(result.getSize())
                .totalItems(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .build();

        // Step 5: Return 200 OK
        return ResponseEntity.ok(body);
    }


    @PreAuthorize("#userId == authentication.name")
    @GetMapping("/{productId}/{userId}")
    public ResponseEntity<ProductDetailDTO> getProductDetail(
            @PathVariable("userId") String userId,
            @PathVariable("productId") Long productId
    ){
        // Step 1: Call the service
        final var result = productReadService.getProduct(productId);
        final var s = result.getProductSummary();
        final var d = result.getProductDetail();

        // Step 2: Map domain -> response DTO
        final var body = ProductDetailDTO.builder()
                .productId(String.valueOf(s.getId()))
                .name(s.getName())
                .unitPriceAmount(s.getUnitPriceAmount())
                .salePriceAmount(s.getSalePriceAmount())
                .manufacturerName(d.getManufacturerName())
                .isImported(d.getIsImported())
                .importCountry(d.getImportCountry())
                .averageRating(s.getAverageRating())
                .reviewCount(s.getReviewCount())
                .stockQty(s.getStockQty())
                .galleryImageUrls(d.getGalleryImageUrls())
                .descriptionImageUrls(d.getDescriptionImageUrls())
                .build();

        // Step 3: Return 200 OK
        return ResponseEntity.ok(body);
    }
}
