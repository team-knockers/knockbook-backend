package com.knockbook.backend.dto;

import com.knockbook.backend.domain.Book;
import com.knockbook.backend.domain.BookSummary;

public final class BookDtoMapper {

    private BookDtoMapper() {
        // Utility 클래스이므로 인스턴스화 방지
    }

    /**
     * domain.Book → dto.BookSummaryDto
     */
    public static BookSummaryDto toSummaryDto(BookSummary b) {
        if (b == null) {
            return null;
        }

        return BookSummaryDto.builder()
                .id(String.valueOf(b.getId()))
                .title(b.getTitle())
                .author(b.getAuthor())
                .publisher(b.getPublisher())
                .publishedAt(b.getPublishedAt())
                .categoryId(String.valueOf(b.getCategoryId()))
                .subcategoryId(String.valueOf(b.getSubcategoryId()))
                .rentalAmount(b.getRentalAmount())
                .purchaseAmount(b.getPurchaseAmount())
                .discountedPurchaseAmount(b.getDiscountedPurchaseAmount())
                .coverThumbnailUrl(b.getCoverThumbnailUrl())
                .rentalAvailability(b.getRentalAvailability().name())
                .purchaseAvailability(b.getPurchaseAvailability().name())
                .viewCount(b.getViewCount())
                .salesCount(b.getSalesCount())
                .rentalCount(b.getRentalCount())
                .averageRating(b.getAverageRating())
                .build();
    }

    /**
     * domain.Book → dto.BookSummaryDto
     */
    public static BookDetailResponse toDetailDto(Book b) {
        if (b == null) {
            return null;
        }

        String pageCountText = b.getPageCount() != null && b.getPageCount() > 0
                ? b.getPageCount() + "p"
                : null;

        String dimensionsText = null;
        if (b.getWidth() != null && b.getWidth() > 0 &&
                b.getHeight() != null && b.getHeight() > 0) {

            if (b.getThickness() != null && b.getThickness() > 0) {
                // width, height, thickness 모두 존재
                dimensionsText = String.format("%d*%d*%dmm", b.getWidth(), b.getHeight(), b.getThickness());
            } else {
                // thickness 없음
                dimensionsText = String.format("%d*%dmm", b.getWidth(), b.getHeight());
            }
        }

        String weightText = b.getWeight() != null && b.getWeight() > 0
                ? b.getWeight() + "g"
                : null;

        String totalVolumesText = b.getTotalVolumes() != null && b.getTotalVolumes() > 0
                ? b.getTotalVolumes() + "권"
                : null;

        return BookDetailResponse.builder()
                .id(String.valueOf(b.getId()))
                .title(b.getTitle())
                .author(b.getAuthor())
                .publisher(b.getPublisher())
                .publishedAt(b.getPublishedAt())
                .categoryId(String.valueOf(b.getCategoryId()))
                .subcategoryId(String.valueOf(b.getSubcategoryId()))
                .introductionTitle(b.getIntroductionTitle())
                .introductionDetail(b.getIntroductionDetail())
                .tableOfContents(b.getTableOfContents())
                .publisherReview(b.getPublisherReview())
                .pageCountText(pageCountText)
                .isbn13(b.getIsbn13())
                .dimensionsText(dimensionsText)
                .weightText(weightText)
                .totalVolumesText(totalVolumesText)
                .rentalAmount(b.getRentalAmount())
                .purchaseAmount(b.getPurchaseAmount())
                .discountedPurchaseAmount(b.getDiscountedPurchaseAmount())
                .coverImageUrl(b.getCoverImageUrl())
                .rentalAvailability(b.getRentalAvailability().name())
                .purchaseAvailability(b.getPurchaseAvailability().name())
                .viewCount(b.getViewCount())
                .salesCount(b.getSalesCount())
                .rentalCount(b.getRentalCount())
                .averageRating(b.getAverageRating())
                .ratingCount(b.getRatingCount())
                .build();
    }
}