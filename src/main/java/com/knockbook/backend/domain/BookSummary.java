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
public class BookSummary {

    public enum Availability { AVAILABLE, OUT_OF_STOCK }

    private Long    id;
    private String  title;
    private String  author;
    private String  publisher;
    private LocalDate publishedAt;
    private Long    categoryId;
    private Long    subcategoryId;
    private Integer rentalAmount;
    private Integer purchaseAmount;
    private Integer discountedPurchaseAmount;
    private String  coverThumbnailUrl;
    private Availability rentalAvailability;
    private Availability purchaseAvailability;
    private Integer viewCount;
    private Integer salesCount;
    private Integer rentalCount;
    private BigDecimal averageRating;

}
