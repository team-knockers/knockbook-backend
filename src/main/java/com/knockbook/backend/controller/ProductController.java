package com.knockbook.backend.controller;

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
        // Step 1: Validate and normalize sorting & paging inputs
        final var allowed = java.util.Set.of(
                "createdAt", "unitPriceAmount", "averageRating", "reviewCount", "name"
        );
        final var sortKey = allowed.contains(sortBy) ? sortBy : "createdAt";
        final var direction = "asc".equalsIgnoreCase(order) ? Sort.Direction.ASC : Sort.Direction.DESC;
        final var safePage = Math.max(1, page) - 1;
        final var safeSize = Math.max(1, size);

        // Step 2: Build Pageable from validated inputs
        final var pageable = PageRequest.of(safePage, safeSize, Sort.by(new Sort.Order(direction, sortKey)));

        // Step 3: Call the service
        final var result = productReadService.getProducts(
                category, searchKeyword, minPrice, maxPrice, pageable
        );

        // Step 4: Map domain -> response DTO
        final var items = result.getContent().stream().map(s -> ProductSummaryDTO.builder()
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
                .products(items)
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
        final var result = productReadService.getProductDetail(productId);

        // Step 2: Map domain -> response DTO
        final var body = ProductDetailDTO.builder()
                .name(result.getName())
                .unitPriceAmount(result.getUnitPriceAmount())
                .salePriceAmount(result.getSalePriceAmount())
                .manufacturerName(result.getManufacturerName())
                .isImported(result.getIsImported())
                .importCountry(result.getImportCountry())
                .averageRating(result.getAverageRating())
                .reviewCount(result.getReviewCount())
                .galleryImageUrls(result.getGalleryImageUrls())
                .descriptionImageUrls(result.getDescriptionImageUrls())
                .build();

        // Step 3: Return 200 OK
        return ResponseEntity.ok(body);
    }
}
