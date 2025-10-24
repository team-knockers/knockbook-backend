package com.knockbook.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "books")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA default constructor
@AllArgsConstructor(access = AccessLevel.PRIVATE) // Builder only
@Builder(toBuilder = true)
public class BookEntity {

    public enum Status { VISIBLE, HIDDEN }
    public enum Availability { AVAILABLE, OUT_OF_STOCK }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "book_id", nullable = false)
    private Long id;

    @Column(name = "title", length = 255, nullable = false)
    private String title;

    @Column(name = "author", length = 255, nullable = false)
    private String author;

    @Column(name = "publisher", length = 255, nullable = false)
    private String publisher;

    @Column(name = "published_at", nullable = false)
    private LocalDate publishedAt;

    @Column(name = "sellable_stock_qty", nullable = false)
    private Integer sellableStockQty;

    @Column(name = "rentable_stock_qty", nullable = false)
    private Integer rentableStockQty;

    @Column(name = "book_category_id", nullable = false)
    private Long bookCategoryId;

    @Column(name = "book_subcategory_id", nullable = false)
    private Long bookSubcategoryId;

    @Column(name = "introduction_title", columnDefinition = "TEXT")
    private String introductionTitle;

    @Column(name = "introduction_detail", columnDefinition = "TEXT")
    private String introductionDetail;

    @Column(name = "table_of_contents", columnDefinition = "TEXT")
    private String tableOfContents;

    @Column(name = "publisher_review", columnDefinition = "TEXT")
    private String publisherReview;

    @Column(name = "isbn13", columnDefinition = "CHAR(13)", nullable = false, unique = true)
    private String isbn13;

    @Column(name = "page_count", nullable = false)
    private Integer pageCount;

    @Column(name = "width", nullable = false)
    private Integer width;

    @Column(name = "height", nullable = false)
    private Integer height;

    @Column(name = "thickness", nullable = false)
    private Integer thickness;

    @Column(name = "weight", nullable = false)
    private Integer weight;

    @Column(name = "total_volumes", nullable = false)
    private Integer totalVolumes;

    @Column(name = "rental_amount", nullable = false)
    private Integer rentalAmount;

    @Column(name = "purchase_amount", nullable = false)
    private Integer purchaseAmount;

    @Column(name = "discounted_purchase_amount", nullable = false)
    private Integer discountedPurchaseAmount;

    @Column(name = "cover_thumbnail_url", length = 2048, nullable = false)
    private String coverThumbnailUrl;

    @Column(name = "cover_image_url", length = 2048, nullable = false)
    private String coverImageUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status;

    @Enumerated(EnumType.STRING)
    @Column(name = "rental_availability", nullable = false)
    private Availability rentalAvailability;

    @Enumerated(EnumType.STRING)
    @Column(name = "purchase_availability", nullable = false)
    private Availability purchaseAvailability;

    @Column(name = "view_count", nullable = false)
    private Integer viewCount;

    @Column(name = "sales_count", nullable = false)
    private Integer salesCount;

    @Column(name = "rental_count", nullable = false)
    private Integer rentalCount;

    @Column(name = "average_rating", precision = 2, scale = 1, nullable = false)
    private BigDecimal averageRating;

    @Column(name = "rating_count", nullable = false)
    private Integer ratingCount;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
    private Instant updatedAt;

    @Column(name = "deleted_at", insertable = false, updatable = true)
    private Instant deletedAt;
}
