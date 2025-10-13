package com.knockbook.backend.entity;

import com.knockbook.backend.domain.OrderItem;
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

    public static OrderItemEntity fromModel(OrderItem item) {
        if (item == null) { return null; }
        return OrderItemEntity.builder()
                .id(item.getId())
                .orderId(item.getOrderId())
                .refType(item.getRefType() == null ? null : RefType.valueOf(item.getRefType().name()))
                .refId(item.getRefId())
                .titleSnapshot(item.getTitleSnapshot())
                .thumbnailUrl(item.getThumbnailUrl())
                .listPriceSnapshot(nz(item.getListPriceSnapshot()))
                .salePriceSnapshot(nz(item.getSalePriceSnapshot()))
                .quantity(nz(item.getQuantity(), 1))
                .rentalDays(nz(item.getRentalDays()))
                .rentalPriceSnapshot(nz(item.getRentalPriceSnapshot()))
                .pointsRate(nz(item.getPointsRate()))
                .pointsEarnedItem(nz(item.getPointsEarnedItem()))
                .lineSubtotalAmount(nz(item.getLineSubtotalAmount()))
                .lineDiscountAmount(nz(item.getLineDiscountAmount()))
                .lineTotalAmount(nz(item.getLineTotalAmount()))
                .build();
    }

    public OrderItem toModel() {
        return OrderItem.builder()
                .id(this.id)
                .orderId(this.orderId)
                .refType(this.refType == null ? null : OrderItem.RefType.valueOf(this.refType.name()))
                .refId(this.refId)
                .titleSnapshot(this.titleSnapshot)
                .thumbnailUrl(this.thumbnailUrl)
                .listPriceSnapshot(nz(this.listPriceSnapshot))
                .salePriceSnapshot(nz(this.salePriceSnapshot))
                .quantity(nz(this.quantity, 1))
                .rentalDays(nz(this.rentalDays))
                .rentalPriceSnapshot(nz(this.rentalPriceSnapshot))
                .pointsRate(nz(this.pointsRate))
                .pointsEarnedItem(nz(this.pointsEarnedItem))
                .lineSubtotalAmount(nz(this.lineSubtotalAmount))
                .lineDiscountAmount(nz(this.lineDiscountAmount))
                .lineTotalAmount(nz(this.lineTotalAmount))
                .build();
    }

    private static int nz(Integer v) { return v == null ? 0 : v; }
    private static int nz(Integer v, int d) { return v == null ? d : v; }
}
