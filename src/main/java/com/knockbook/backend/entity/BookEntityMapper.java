package com.knockbook.backend.entity;

import com.knockbook.backend.domain.Book;
import com.knockbook.backend.domain.BookSummary;

public final class BookEntityMapper {

    private BookEntityMapper() {
        // Utility 클래스이므로 인스턴스화 방지
    }

    /**
     * entity.BookEntity → domain.Book
     */
    public static Book toDomain(BookEntity e) {
        if (e == null) {
            return null;
        }

        return Book.builder()
                .id(e.getId())
                .title(e.getTitle())
                .author(e.getAuthor())
                .publisher(e.getPublisher())
                .publishedAt(e.getPublishedAt())
                .sellableStockQty(e.getSellableStockQty())
                .rentableStockQty(e.getRentableStockQty())
                .categoryId(e.getBookCategoryId())
                .subcategoryId(e.getBookSubcategoryId())
                .introductionTitle(e.getIntroductionTitle())
                .introductionDetail(e.getIntroductionDetail())
                .tableOfContents(e.getTableOfContents())
                .publisherReview(e.getPublisherReview())
                .isbn13(e.getIsbn13())
                .pageCount(e.getPageCount())
                .width(e.getWidth())
                .height(e.getHeight())
                .thickness(e.getThickness())
                .weight(e.getWeight())
                .totalVolumes(e.getTotalVolumes())
                .rentalAmount(e.getRentalAmount())
                .purchaseAmount(e.getPurchaseAmount())
                .discountedPurchaseAmount(e.getDiscountedPurchaseAmount())
                .coverThumbnailUrl(e.getCoverThumbnailUrl())
                .coverImageUrl(e.getCoverImageUrl())
                .status(Book.Status.valueOf(e.getStatus().name()))
                .rentalAvailability(Book.Availability.valueOf(e.getRentalAvailability().name()))
                .purchaseAvailability(Book.Availability.valueOf(e.getPurchaseAvailability().name()))
                .viewCount(e.getViewCount())
                .salesCount(e.getSalesCount())
                .rentalCount(e.getRentalCount())
                .averageRating(e.getAverageRating())
                .ratingCount(e.getRatingCount())
                .build();
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