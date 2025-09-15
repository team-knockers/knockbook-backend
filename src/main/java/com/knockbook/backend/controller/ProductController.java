package com.knockbook.backend.controller;

import com.knockbook.backend.domain.ProductDetail;
import com.knockbook.backend.domain.ProductSummary;
import com.knockbook.backend.dto.GetProductDetailResponse;
import com.knockbook.backend.dto.GetProductsResponse;
import com.knockbook.backend.dto.ProductDetailDTO;
import com.knockbook.backend.dto.ProductSummaryDTO;
import com.knockbook.backend.service.ProductReadService;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
@Validated
public class ProductController {
    private final ProductReadService productReadService;

    @PreAuthorize("#userId == authentication.name")
    @GetMapping("/{userId}")
    public ResponseEntity<GetProductsResponse> getProducts(
            @PathVariable("userId") String userId,
            @RequestParam String category,
            @RequestParam String sort,
            @RequestParam(required = false) String searchKeyword,
            @RequestParam(required = false) Integer minPrice,
            @RequestParam(required = false) Integer maxPrice,
            @RequestParam @Min(1)int page,
            @RequestParam @Min(1) int size
            ) {
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
                .page(result.getNumber() + 1)
                .size(result.getSize())
                .totalItems(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .hasNext(result.hasNext())
                .hasPrevious(result.hasPrevious())
                .build();

        return ResponseEntity.ok(body);
    }


    @PreAuthorize("#userId == authentication.name")
    @GetMapping("/{productId}/{userId}")
    public ResponseEntity<GetProductDetailResponse> getProductDetail(
            @PathVariable("userId") String userId,
            @PathVariable("productId") Long productId
    ){
        Optional<ProductDetail> result = productReadService.getProductDetail(productId);
        if(result.isEmpty()){
            return ResponseEntity.notFound().build();
        }

        ProductDetail d = result.get();
        ProductDetailDTO item = ProductDetailDTO.builder()
                .name(d.getName())
                .unitPriceAmount(d.getUnitPriceAmount())
                .salePriceAmount(d.getSalePriceAmount())
                .manufacturerName(d.getManufacturerName())
                .isImported(d.getIsImported())
                .importCountry(d.getImportCountry())
                .averageRating(d.getAverageRating())
                .reviewCount(d.getReviewCount())
                .galleryImageUrls(d.getGalleryImageUrls())
                .descriptionImageUrls(d.getDescriptionImageUrls())
                .build();

        GetProductDetailResponse body = GetProductDetailResponse.builder()
                .product(item).build();

        return ResponseEntity.ok(body);
    }
}
