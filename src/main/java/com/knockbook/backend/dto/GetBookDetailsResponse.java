package com.knockbook.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetBookDetailsResponse {

    private String id;
    private String title;
    private String author;
    private String publisher;
    private LocalDate publishedAt;
    private String categoryId;
    private String subcategoryId;
    private String introductionTitle;
    private String introductionDetail;
    private String tableOfContents;
    private String publisherReview;
    private String isbn13;
    private String pageCountText;
    private String dimensionsText;
    private String weightText;
    private String totalVolumesText;
    private Integer rentalAmount;
    private Integer purchaseAmount;
    private Integer discountedPurchaseAmount;
    private String coverImageUrl;
    private String rentalAvailability;
    private String purchaseAvailability;
    private Integer viewCount;
    private Integer salesCount;
    private Integer rentalCount;
    private Double averageRating;
    private Integer ratingCount;
    private Integer rentalPoint;
    private Integer purchasePoint;
}
