package com.knockbook.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "order_items")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class OrderItemEntity {

    public enum RefType { BOOK_PURCHASE, BOOK_RENTAL, PRODUCT }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Enumerated(EnumType.STRING)
    @Column(name = "ref_type", nullable = false)
    private RefType refType;

    @Column(name = "ref_id", nullable = false)
    private Long refId;

    @Column(name = "title_snapshot", nullable = false)
    private String titleSnapshot;

    @Column(name = "thumbnail_url")
    private String thumbnailUrl;

    @Column(name = "list_price_snapshot")
    private Integer listPriceSnapshot;

    @Column(name = "sale_price_snapshot")
    private Integer salePriceSnapshot;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "rental_days", nullable = false)
    private Integer rentalDays;

    @Column(name = "rental_price_snapshot")
    private Integer rentalPriceSnapshot;

    @Column(name = "points_rate", nullable = false)
    private Integer pointsRate;

    @Column(name = "points_earned_item", nullable = false)
    private Integer pointsEarnedItem;

    @Column(name = "line_subtotal_amount", nullable = false)
    private Integer lineSubtotalAmount;

    @Column(name = "line_discount_amount", nullable = false)
    private Integer lineDiscountAmount;

    @Column(name = "line_total_amount", nullable = false)
    private Integer lineTotalAmount;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime updatedAt;
}
