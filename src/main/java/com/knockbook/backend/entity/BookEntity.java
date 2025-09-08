package com.knockbook.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "books")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA default constructor
@AllArgsConstructor(access = AccessLevel.PRIVATE) // Builder only
@Builder
public class BookEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "book_id", nullable = false)
    private Long bookId;

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

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
    private Instant updatedAt;

    @Column(name = "deleted_at", insertable = false, updatable = true)
    private Instant deletedAt;

}