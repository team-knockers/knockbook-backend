package com.knockbook.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "products")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class ProductEntity {
    public enum Status { ACTIVE, HIDDEN, DISCONTINUED }
    public enum Availability { AVAILABLE, OUT_OF_STOCK, PREORDER, BACKORDER, COMING_SOON, SOLD_OUT }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "category_id", nullable = false)
    private Long categoryId;

    @Column(name = "sku", length = 64, nullable = false)
    private String sku;

    @Column(name = "name", length = 200, nullable = false)
    private String name;

    @Column(name = "stock_qty", nullable = false)
    private Integer stockQty;

    @Column(name = "unit_price_amount", nullable = false)
    private Integer unitPriceAmount;

    @Column(name = "manufacturer_name", length = 100, nullable = false )
    private String manufacturerName;

    @Column(name = "is_imported", length = 100, nullable = false)
    private String isImported;

    @Column(name = "import_country", length = 100, nullable = false)
    private String importCountry;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Column(name = "released_at")
    private Instant releasedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status;

    @Enumerated(EnumType.STRING)
    @Column(name = "availability", nullable = false)
    private Availability availability;
}
