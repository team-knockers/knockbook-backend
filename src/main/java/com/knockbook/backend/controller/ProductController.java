package com.knockbook.backend.controller;

import com.knockbook.backend.domain.ProductReviewSortBy;
import com.knockbook.backend.domain.ProductSortBy;
import com.knockbook.backend.domain.SortOrder;
import com.knockbook.backend.dto.*;
import com.knockbook.backend.service.ProductService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
@Validated
public class ProductController {
    private final ProductService productService;

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
        final long userIdLong = Long.parseLong(userId);

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
        final var result = productService.getProductList(
                category, searchKeyword, minPrice, maxPrice, pageable, userIdLong
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
                .wishedByMe(s.getWishedByMe())
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
            @PathVariable("productId") Long productId,
            @PathVariable("userId") String userId
    ){
        final long userIdLong = Long.parseLong(userId);

        // Step 1: Call the service
        final var result = productService.getProduct(productId, userIdLong);
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
                .wishedByMe(Boolean.TRUE.equals(s.getWishedByMe()))
                .galleryImageUrls(d.getGalleryImageUrls())
                .descriptionImageUrls(d.getDescriptionImageUrls())
                .build();

        // Step 3: Return 200 OK
        return ResponseEntity.ok(body);
    }

    @PreAuthorize("#userId == authentication.name")
    @GetMapping("/{productId}/reviews/{userId}")
    public ResponseEntity<GetProductReviewsResponse> getProductReviews(
            @PathVariable("productId") Long productId,
            @PathVariable("userId") String userId,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String order,
            @RequestParam @Min(1) int page,
            @RequestParam @Min(1) int size
    ){
        final long userIdLong = Long.parseLong(userId);

        final var safePage = Math.max(1, page) - 1;
        final var safeSize = Math.max(1, size);

        final var sortKeyEnum = ProductReviewSortBy.parseOrDefault(sortBy);
        final var sortOrderEnum = SortOrder.parseOrDefault(order);

        final var sortDirection = (sortOrderEnum == SortOrder.asc) ? Sort.Direction.ASC : Sort.Direction.DESC;
        final var sortSpec      = Sort.by(new Sort.Order(sortDirection, sortKeyEnum.name()));
        final var pageable      = PageRequest.of(safePage, safeSize, sortSpec);

        final var result = productService.getProductReviews(productId, userIdLong, pageable);

        final var productReviews = result.getProductReviews().stream()
                .map(r -> ProductReviewDTO.builder()
                        .reviewId(String.valueOf(r.getReviewId()))
                        .displayName(r.getDisplayName())
                        .body(r.getBody())
                        .rating(r.getRating())
                        .createdAt(r.getCreatedAt().toString()) 
                        .likesCount(r.getLikesCount())
                        .likedByMe(r.isLikedByMe())
                        .build())
                .toList();


        final var body = GetProductReviewsResponse.builder()
                .productReviews(productReviews)
                .page(result.getPage())
                .size(result.getSize())
                .totalItems(result.getStats().getTotalItems())
                .totalPages(result.getTotalPages())
                .averageRating(result.getStats().getAverageRating())
                .starCounts(result.getStats().getStarCounts())
                .build();

        return ResponseEntity.ok(body);
    }

    @PreAuthorize("#userId == authentication.name")
    @PostMapping("/{productId}/reviews/{userId}")
    public ResponseEntity<ProductReviewDTO> createReview(
            @PathVariable("productId") Long productId,
            @PathVariable("userId") String userId,
            @RequestBody @Valid CreateProductReviewRequest req
    ) {
        final long userIdLong = Long.parseLong(userId);
        final var productReview = productService.createReview(productId, userIdLong, req);
        final var body = ProductReviewDTO.builder()
                .reviewId(String.valueOf(productReview.getReviewId()))
                .displayName(productReview.getDisplayName())
                .body(productReview.getBody())
                .rating(productReview.getRating())
                .createdAt(productReview.getCreatedAt().toString())
                .likesCount(productReview.getLikesCount())
                .build();

        final var location = URI.create(
                "/products/reviews/%s".formatted(body.getReviewId())
        );

        return ResponseEntity.created(location).body(body);
    }

    @PreAuthorize("#userId == authentication.name")
    @DeleteMapping("/{productId}/reviews/{reviewId}/{userId}")
    public ResponseEntity<Void> deleteReview(
            @PathVariable("productId") Long productId,
            @PathVariable("reviewId") Long reviewId,
            @PathVariable("userId") String userId
    ) {
        final long userIdLong = Long.parseLong(userId);
        productService.deleteReview(productId, reviewId, userIdLong);

        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("#userId == authentication.name")
    @GetMapping("/{productId}/inquiries/{userId}")
    public ResponseEntity<GetProductInquiriesResponse> getProductInquiries(
            @PathVariable("productId") Long productId,
            @PathVariable("userId") String userId,
            @RequestParam @Min(1) int page,
            @RequestParam @Min(1) int size
    ) {
        final var safePage = Math.max(1, page) - 1;
        final var safeSize = Math.max(1, size);
        final var pageable = PageRequest.of(safePage, safeSize);

        final var result = productService.getProductInquiries(productId, pageable);
        final var productInquiries = result.getContent().stream()
                .map(i -> ProductInquiryDTO.builder()
                        .inquiryId(String.valueOf(i.getInquiryId()))
                        .displayName(i.getDisplayName())
                        .title(i.getTitle())
                        .questionBody(i.getQuestionBody())
                        .createdAt(i.getCreatedAt().toString())
                        .answerBody(i.getAnswerBody())
                        .answeredAt(i.getAnsweredAt() == null ? null : i.getAnsweredAt().toString())
                        .status((i.getAnsweredAt() != null) ? "ANSWERED" : "WAITING")
                        .build())
                .toList();

        final var body = GetProductInquiriesResponse.builder()
                .productInquiries(productInquiries)
                .page(result.getNumber() + 1)
                .totalPages(result.getTotalPages())
                .build();

        return ResponseEntity.ok(body);
    }

    @PreAuthorize("#userId == authentication.name")
    @PutMapping("/reviews/{reviewId}/likes/{userId}")
    public ResponseEntity<Void> likeReview(
            @PathVariable("reviewId") Long reviewId,
            @PathVariable("userId") String userId
    ) {
        final var userIdLong = Long.parseLong(userId);
        productService.likeReview(reviewId, userIdLong);

        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("#userId == authentication.name")
    @DeleteMapping("/reviews/{reviewId}/likes/{userId}")
    public ResponseEntity<Void> unlikeReview(
            @PathVariable("reviewId") Long reviewId,
            @PathVariable("userId") String userId
    ) {
        final var userIdLong = Long.parseLong(userId);
        productService.unlikeReview(reviewId, userIdLong);

        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("#userId == authentication.name")
    @PostMapping("/{productId}/inquiries/{userId}")
    public ResponseEntity<Void> createInquiry(
            @PathVariable("productId") Long productId,
            @PathVariable("userId") String userId,
            @RequestBody @Valid CreateProductInquiryRequest req
    ) {
        final var userIdLong = Long.parseLong(userId);
        final var inquiryId = productService.createInquiry(productId, userIdLong, req);
        final var location = URI.create(
                "/products/%d/inquiries/%s/%d".formatted(productId, userId, inquiryId)
        );

        return ResponseEntity.created(location).build();
    }

    @PreAuthorize("#userId == authentication.name")
    @PutMapping("/wishes/{productId}/{userId}")
    public ResponseEntity<Void> addToWishlist(
            @PathVariable("productId") Long productId,
            @PathVariable("userId") String userId
    ) {
        final var userIdLong = Long.parseLong(userId);
        productService.addToWishlist(productId, userIdLong);

        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("#userId == authentication.name")
    @DeleteMapping("/wishes/{productId}/{userId}")
    public ResponseEntity<Void> removeFromWishlist(
            @PathVariable("productId") Long productId,
            @PathVariable("userId") String userId
    ) {
        final var userIdLong = Long.parseLong(userId);
        productService.removeFromWishlist(productId, userIdLong);

        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("#userId == authentication.name")
    @GetMapping("/wishes/{userId}")
    public ResponseEntity<GetProductWishlist> getProductWishlist(
            @PathVariable("userId") String userId
    ) {
        final var userIdLong = Long.parseLong(userId);
        final var result = productService.getProductWishlist(userIdLong);

        final var products = result.getProducts().stream().map(s -> ProductSummaryDTO.builder()
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

        final var body = GetProductWishlist.builder()
                .products(products)
                .build();

        return ResponseEntity.ok(body);
    }
}
