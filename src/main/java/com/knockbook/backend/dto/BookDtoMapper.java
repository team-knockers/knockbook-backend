package com.knockbook.backend.dto;

import com.knockbook.backend.domain.Book;
import com.knockbook.backend.domain.BookSummary;
import com.knockbook.backend.domain.CartItem;
import com.knockbook.backend.domain.PointsPolicy;

import java.math.BigDecimal;

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
                .averageRating(roundToFirstDecimal(b.getAverageRating()))
                .build();
    }

    /**
     * domain.Book → dto.BookSummaryDto
     */
    public static GetBookDetailsResponse toDetailDto(Book b) {
        if (b == null) {
            return null;
        }

        final var pageCountText = b.getPageCount() != null && b.getPageCount() > 0
                ? b.getPageCount() + "p"
                : null;

        final var dimensionsText = getDimensionsText(b);

        final var  weightText = b.getWeight() != null && b.getWeight() > 0
                ? b.getWeight() + "g"
                : null;

        final var totalVolumesText = b.getTotalVolumes() != null && b.getTotalVolumes() > 0
                ? b.getTotalVolumes() + "권"
                : null;

        final var rentalPointRate = PointsPolicy.of(CartItem.RefType.BOOK_RENTAL);
        final var purchasePointRate = PointsPolicy.of(CartItem.RefType.BOOK_PURCHASE);

        final var rentalPoint = b.getRentalAmount() != null
                ? (int) Math.floor(b.getRentalAmount() * rentalPointRate / 100.0)
                : 0;
        final var purchasePoint = b.getDiscountedPurchaseAmount() != null
                ? (int) Math.floor(b.getDiscountedPurchaseAmount() * purchasePointRate / 100.0)
                : 0;

        return GetBookDetailsResponse.builder()
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
                .averageRating(roundToFirstDecimal(b.getAverageRating()))
                .ratingCount(b.getRatingCount())
                .purchasePoint(purchasePoint)
                .rentalPoint(rentalPoint)
                .build();
    }

    /**
     * dto.BookCreateRequest → domain.Book
     */
    public static Book toDomainForCreate(BookCreateRequest req) {
        if (req == null) return null;

        return Book.builder()
                .title(req.getTitle())
                .author(req.getAuthor())
                .publisher(req.getPublisher())
                .publishedAt(req.getPublishedAt())
                .sellableStockQty(req.getSellableStockQty())
                .rentableStockQty(req.getRentableStockQty())
                .categoryId(Long.valueOf(req.getCategoryId()))
                .subcategoryId(Long.valueOf(req.getSubcategoryId()))
                .introductionTitle(req.getIntroductionTitle())
                .introductionDetail(req.getIntroductionDetail())
                .tableOfContents(req.getTableOfContents())
                .publisherReview(req.getPublisherReview())
                .isbn13(req.getIsbn13())
                .pageCount(req.getPageCount() != null ? req.getPageCount() : 0)
                .width(req.getWidth() != null ? req.getWidth() : 0)
                .height(req.getHeight() != null ? req.getHeight() : 0)
                .thickness(req.getThickness() != null ? req.getThickness() : 0)
                .weight(req.getWeight() != null ? req.getWeight() : 0)
                .totalVolumes(req.getTotalVolumes() != null ? req.getTotalVolumes() : 1)
                .rentalAmount(req.getRentalAmount())
                .purchaseAmount(req.getPurchaseAmount())
                .discountedPurchaseAmount(req.getDiscountedPurchaseAmount())
                .coverThumbnailUrl(req.getCoverThumbnailUrl())
                .coverImageUrl(req.getCoverImageUrl())
                .status(req.getStatus() != null ? Book.Status.valueOf(req.getStatus().name()) : Book.Status.VISIBLE)
                .build();
    }

    /**
     * PATCH: Map BookUpdateRequest to domain.Book.
     * Null fields are ignored and existing values are preserved.
     */
    public static Book toDomainForPatch(BookUpdateRequest req, Book existing) {
        if (req == null || existing == null) {
            throw new IllegalArgumentException("Request and existing Book must not be null");
        }

        return existing.toBuilder()
                .title(req.getTitle() != null ? req.getTitle() : existing.getTitle())
                .author(req.getAuthor() != null ? req.getAuthor() : existing.getAuthor())
                .publisher(req.getPublisher() != null ? req.getPublisher() : existing.getPublisher())
                .publishedAt(req.getPublishedAt() != null ? req.getPublishedAt() : existing.getPublishedAt())
                .sellableStockQty(req.getSellableStockQty() != null ? req.getSellableStockQty() : existing.getSellableStockQty())
                .rentableStockQty(req.getRentableStockQty() != null ? req.getRentableStockQty() : existing.getRentableStockQty())
                .categoryId(req.getCategoryId() != null ? Long.valueOf(req.getCategoryId()) : existing.getCategoryId())
                .subcategoryId(req.getSubcategoryId() != null ? Long.valueOf(req.getSubcategoryId()) : existing.getSubcategoryId())
                .introductionTitle(req.getIntroductionTitle() != null ? req.getIntroductionTitle() : existing.getIntroductionTitle())
                .introductionDetail(req.getIntroductionDetail() != null ? req.getIntroductionDetail() : existing.getIntroductionDetail())
                .tableOfContents(req.getTableOfContents() != null ? req.getTableOfContents() : existing.getTableOfContents())
                .publisherReview(req.getPublisherReview() != null ? req.getPublisherReview() : existing.getPublisherReview())
                .isbn13(req.getIsbn13() != null ? req.getIsbn13() : existing.getIsbn13())
                .pageCount(req.getPageCount() != null ? req.getPageCount() : existing.getPageCount())
                .width(req.getWidth() != null ? req.getWidth() : existing.getWidth())
                .height(req.getHeight() != null ? req.getHeight() : existing.getHeight())
                .thickness(req.getThickness() != null ? req.getThickness() : existing.getThickness())
                .weight(req.getWeight() != null ? req.getWeight() : existing.getWeight())
                .totalVolumes(req.getTotalVolumes() != null ? req.getTotalVolumes() : existing.getTotalVolumes())
                .rentalAmount(req.getRentalAmount() != null ? req.getRentalAmount() : existing.getRentalAmount())
                .purchaseAmount(req.getPurchaseAmount() != null ? req.getPurchaseAmount() : existing.getPurchaseAmount())
                .discountedPurchaseAmount(req.getDiscountedPurchaseAmount() != null ? req.getDiscountedPurchaseAmount() : existing.getDiscountedPurchaseAmount())
                .coverThumbnailUrl(req.getCoverThumbnailUrl() != null ? req.getCoverThumbnailUrl() : existing.getCoverThumbnailUrl())
                .coverImageUrl(req.getCoverImageUrl() != null ? req.getCoverImageUrl() : existing.getCoverImageUrl())
                .status(req.getStatus() != null ? Book.Status.valueOf(req.getStatus().name()) : existing.getStatus())
                .build();
    }

    private static String getDimensionsText(Book b) {
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
        return dimensionsText;
    }

    /**
     * Rounds the given BigDecimal value to one decimal place and returns it as a double.
     * Example: 4.26 -> 4.3, 3.94 -> 3.9
     */
    private static double roundToFirstDecimal(BigDecimal value) {
        if (value == null) return 0.0;
        return Math.round(value.doubleValue() * 10) / 10.0;
    }
}
