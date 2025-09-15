package com.knockbook.backend.entity;

import com.knockbook.backend.domain.Book;
import com.knockbook.backend.domain.BookSummary;

public final class BookEntityMapper {

    private BookEntityMapper() {
        // Utility 클래스이므로 인스턴스화 방지
    }

    /**
     * domain.Book → entity.BookEntity
     */
    public static BookEntity toEntity(Book b) {
        if (b == null) {
            return null;
        }

        return BookEntity.builder()
                .id(b.getId())
                .title(b.getTitle())
                .author(b.getAuthor())
                .publisher(b.getPublisher())
                .publishedAt(b.getPublishedAt())
                .sellableStockQty(b.getSellableStockQty())
                .rentableStockQty(b.getRentableStockQty())
                .bookCategoryId(b.getCategoryId())
                .bookSubcategoryId(b.getSubcategoryId())
                .introductionTitle(b.getIntroductionTitle())
                .introductionDetail(b.getIntroductionDetail())
                .tableOfContents(b.getTableOfContents())
                .publisherReview(b.getPublisherReview())
                .isbn13(b.getIsbn13())
                .pageCount(b.getPageCount())
                .width(b.getWidth())
                .height(b.getHeight())
                .thickness(b.getThickness())
                .weight(b.getWeight())
                .totalVolumes(b.getTotalVolumes())
                .rentalAmount(b.getRentalAmount())
                .purchaseAmount(b.getPurchaseAmount())
                .discountedPurchaseAmount(b.getDiscountedPurchaseAmount())
                .coverThumbnailUrl(b.getCoverThumbnailUrl())
                .coverImageUrl(b.getCoverImageUrl())
                .status(BookEntity.Status.valueOf(b.getStatus().name()))
                .rentalAvailability(BookEntity.Availability.valueOf(b.getRentalAvailability().name()))
                .purchaseAvailability(BookEntity.Availability.valueOf(b.getPurchaseAvailability().name()))
                .viewCount(b.getViewCount())
                .salesCount(b.getSalesCount())
                .rentalCount(b.getRentalCount())
                .averageRating(b.getAverageRating())
                .ratingCount(b.getRatingCount())
                .build();
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
