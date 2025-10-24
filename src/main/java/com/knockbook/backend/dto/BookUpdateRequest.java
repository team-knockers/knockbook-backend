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
public class BookUpdateRequest {

    public enum Status { VISIBLE, HIDDEN }

    private String title;
    private String author;
    private String publisher;
    private LocalDate publishedAt;
    private Integer sellableStockQty;
    private Integer rentableStockQty;
    private String categoryId;
    private String subcategoryId;
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
    private Status status;
}