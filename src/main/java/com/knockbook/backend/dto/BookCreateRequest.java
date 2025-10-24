package com.knockbook.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookCreateRequest {

    public enum Status { VISIBLE, HIDDEN }

    @NotBlank
    private String title;

    @NotBlank
    private String author;

    @NotBlank
    private String publisher;

    @NotNull
    private LocalDate publishedAt;

    @NotNull
    private Integer sellableStockQty;

    @NotNull
    private Integer rentableStockQty;

    @NotBlank
    private String categoryId;

    @NotBlank
    private String subcategoryId;

    private String introductionTitle;
    private String introductionDetail;
    private String tableOfContents;
    private String publisherReview;

    @NotBlank
    @Size(min = 13, max = 13)
    @Pattern(regexp = "\\d{13}")
    private String isbn13;

    private Integer pageCount;
    private Integer width;
    private Integer height;
    private Integer thickness;
    private Integer weight;
    private Integer totalVolumes;

    @NotNull
    private Integer rentalAmount;
    @NotNull
    private Integer purchaseAmount;
    @NotNull
    private Integer discountedPurchaseAmount;
    @NotBlank
    private String coverThumbnailUrl;
    @NotBlank
    private String coverImageUrl;

    private Status status;
}
