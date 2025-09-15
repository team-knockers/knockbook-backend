package com.knockbook.backend.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Book {

    public enum Status { VISIBLE, HIDDEN }
    public enum Availability { AVAILABLE, OUT_OF_STOCK }

    private Long id;
    private String title;
    private String author;
    private String publisher;
    private LocalDate publishedAt;
    private Integer sellableStockQty;
    private Integer rentableStockQty;
    private Long categoryId;
    private Long subcategoryId;
    private String introductionTitle;
    private String introductionDetail;
    private String tableOfContents;
    private String publisherReview;
    private String isbn13;
    private Integer pageCount;
    private Integer width;
    private Integer height;
    private Integer thickness;
    private Integer weight;
    private Integer totalVolumes;
    private Integer rentalAmount;
    private Integer purchaseAmount;
    private Integer discountedPurchaseAmount;
    private String coverThumbnailUrl;
    private String coverImageUrl;
    private Availability rentalAvailability;
    private Availability purchaseAvailability;
    private Status status;
    private Integer viewCount;
    private Integer salesCount;
    private Integer rentalCount;
    private BigDecimal averageRating;
    private Integer ratingCount;
}