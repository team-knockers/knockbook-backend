package com.knockbook.backend.entity;

import com.knockbook.backend.domain.BookSummary;

public final class BookEntityMapper {

    private BookEntityMapper() {
        // Utility 클래스이므로 인스턴스화 방지
    }

    /**
     * entity.BookEntity → domain.BookSummary
     */
    public static BookSummary toSummaryDomain(BookEntity e) {
        if (e == null) {
            return null;
        }

        return BookSummary.builder()
                .id(e.getId())
                .title(e.getTitle())
                .author(e.getAuthor())
                .publisher(e.getPublisher())
                .publishedAt(e.getPublishedAt())
                .categoryId(e.getBookCategoryId())
                .subcategoryId(e.getBookSubcategoryId())
                .rentalAmount(e.getRentalAmount())
                .purchaseAmount(e.getPurchaseAmount())
                .discountedPurchaseAmount(e.getDiscountedPurchaseAmount())
                .coverThumbnailUrl(e.getCoverThumbnailUrl())
                .rentalAvailability(BookSummary.Availability.valueOf(e.getRentalAvailability().name()))
                .purchaseAvailability(BookSummary.Availability.valueOf(e.getPurchaseAvailability().name()))
                .viewCount(e.getViewCount())
                .salesCount(e.getSalesCount())
                .rentalCount(e.getRentalCount())
                .averageRating(e.getAverageRating())
                .build();
    }
}
