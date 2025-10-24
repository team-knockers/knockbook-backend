package com.knockbook.backend.entity;

import com.knockbook.backend.domain.Book;
import com.knockbook.backend.domain.BookSummary;

import java.math.BigDecimal;

public final class BookEntityMapper {

    private BookEntityMapper() {
        // Utility 클래스이므로 인스턴스화 방지
    }

    /**
     * domain.Book → entity.BookEntity
     * Create new entity with default values
     */
    public static BookEntity toBookEntityForInsert(Book d) {
        if (d == null) {
            return null;
        }

        return BookEntity.builder()
                .title(d.getTitle())
                .author(d.getAuthor())
                .publisher(d.getPublisher())
                .publishedAt(d.getPublishedAt())
                .sellableStockQty(d.getSellableStockQty())
                .rentableStockQty(d.getRentableStockQty())
                .bookCategoryId(d.getCategoryId())
                .bookSubcategoryId(d.getSubcategoryId())
                .introductionTitle(d.getIntroductionTitle())
                .introductionDetail(d.getIntroductionDetail())
                .tableOfContents(d.getTableOfContents())
                .publisherReview(d.getPublisherReview())
                .isbn13(d.getIsbn13())
                .pageCount(d.getPageCount())
                .width(d.getWidth())
                .height(d.getHeight())
                .thickness(d.getThickness())
                .weight(d.getWeight())
                .totalVolumes(d.getTotalVolumes())
                .rentalAmount(d.getRentalAmount())
                .purchaseAmount(d.getPurchaseAmount())
                .discountedPurchaseAmount(d.getDiscountedPurchaseAmount())
                .coverThumbnailUrl(d.getCoverThumbnailUrl())
                .coverImageUrl(d.getCoverImageUrl())
                .status(BookEntity.Status.valueOf(d.getStatus().name()))
                // set default values
                .viewCount(0)
                .salesCount(0)
                .rentalCount(0)
                .averageRating(BigDecimal.ZERO)
                .ratingCount(0)
                .rentalAvailability(d.getRentableStockQty() > 0
                        ? BookEntity.Availability.AVAILABLE
                        : BookEntity.Availability.OUT_OF_STOCK)
                .purchaseAvailability(d.getSellableStockQty() > 0
                        ? BookEntity.Availability.AVAILABLE
                        : BookEntity.Availability.OUT_OF_STOCK)
                .build();
    }

    /**
     * domain.Book → entity.BookEntity
     * Update existing entity (partial overwrite)
     */
    public static BookEntity toBookEntityForPatch(Book d, BookEntity e) {
        if (d == null || e == null) {
            return e;
        }

        return e.toBuilder()
                .title(d.getTitle())
                .author(d.getAuthor())
                .publisher(d.getPublisher())
                .publishedAt(d.getPublishedAt())
                .sellableStockQty(d.getSellableStockQty())
                .rentableStockQty(d.getRentableStockQty())
                .bookCategoryId(d.getCategoryId())
                .bookSubcategoryId(d.getSubcategoryId())
                .introductionTitle(d.getIntroductionTitle())
                .introductionDetail(d.getIntroductionDetail())
                .tableOfContents(d.getTableOfContents())
                .publisherReview(d.getPublisherReview())
                .isbn13(d.getIsbn13())
                .pageCount(d.getPageCount())
                .width(d.getWidth())
                .height(d.getHeight())
                .thickness(d.getThickness())
                .weight(d.getWeight())
                .totalVolumes(d.getTotalVolumes())
                .rentalAmount(d.getRentalAmount())
                .purchaseAmount(d.getPurchaseAmount())
                .discountedPurchaseAmount(d.getDiscountedPurchaseAmount())
                .coverThumbnailUrl(d.getCoverThumbnailUrl())
                .coverImageUrl(d.getCoverImageUrl())
                .status(BookEntity.Status.valueOf(d.getStatus().name()))
                .purchaseAvailability(d.getSellableStockQty() > 0
                        ? BookEntity.Availability.AVAILABLE
                        : BookEntity.Availability.OUT_OF_STOCK)
                .rentalAvailability(d.getRentableStockQty() > 0
                        ? BookEntity.Availability.AVAILABLE
                        : BookEntity.Availability.OUT_OF_STOCK)
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
