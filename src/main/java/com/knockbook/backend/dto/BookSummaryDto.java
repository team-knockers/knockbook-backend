package com.knockbook.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookSummaryDto {

    private String id;
    private String title;
    private String author;
    private String publisher;
    private LocalDate publishedAt;
    private String categoryId;
    private String subcategoryId;
    private Integer rentalAmount;
    private Integer purchaseAmount;
    private Integer discountedPurchaseAmount;
    private String coverThumbnailUrl;
    private String rentalAvailability;
    private String purchaseAvailability;
    private Integer viewCount;
    private Integer salesCount;
    private Integer rentalCount;
    private BigDecimal averageRating;

}
