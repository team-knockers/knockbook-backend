package com.knockbook.backend.dto;

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

}
