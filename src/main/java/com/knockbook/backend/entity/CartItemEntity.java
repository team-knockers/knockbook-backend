package com.knockbook.backend.entity;

import com.knockbook.backend.domain.CartItem;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "cart_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class CartItemEntity {

    public enum RefType { BOOK_PURCHASE, BOOK_RENTAL, PRODUCT }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "cart_id", nullable = false)
    private Long cartId;

    @Enumerated(EnumType.STRING)
    @Column(name = "ref_type", nullable = false)
    private RefType refType;

    @Column(name = "ref_id", nullable = false)
    private Long refId;

    @Column(name = "title_snapshot", nullable = false, updatable = false)
    private String titleSnapshot;

    @Column(name = "thumbnail_url", nullable = false)
    private String thumbnailUrl;

    @Column(name="list_price_snapshot", updatable=false)
    private Integer listPrice; // KRW

    @Column(name="sale_price_snapshot", updatable=false)
    private Integer salePrice;

    @Column(name="rental_days", nullable=false)
    private int rentalDays;

    @Column(name="rental_price_snapshot", updatable=false)
    private Integer rentalPrice;

    @Column(name="quantity", nullable=false)
    private Integer quantity;

    @Column(name="points_rate", nullable=false)
    private Integer pointsRate;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
    private Instant updatedAt;

    public CartItem toDomain() {
        return CartItem.builder()
                .id(id)
                .cartId(cartId)
                .refType(CartItem.RefType.valueOf(refType.name()))
                .refId(refId)
                .titleSnapshot(titleSnapshot)
                .thumbnailUrl(thumbnailUrl)
                .listPriceSnapshot(listPrice)
                .salePriceSnapshot(salePrice)
                .rentalDays(rentalDays)
                .rentalPriceSnapshot(rentalPrice)
                .quantity(quantity)
                .pointsRate(pointsRate)
                .build();
    }
}
