package com.knockbook.backend.entity;

import com.knockbook.backend.domain.OrderAggregate;
import com.knockbook.backend.domain.OrderItem;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class OrderEntity {

    private static final ZoneId ZONE_SEOUL = ZoneId.of("Asia/Seoul");

    public enum OrderStatus { PENDING, FULFILLING, COMPLETED, CANCELLED }
    public enum PaymentStatus { READY, PAID, PARTIAL_REFUNDED, REFUNDED, FAILED, CANCELLED }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_no")
    private String orderNo;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "cart_id")
    private Long cartId;

    @Column(name = "shipping_address_id")
    private Long shippingAddressId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    private PaymentStatus paymentStatus;

    @Column(name = "item_count", nullable = false)
    private Integer itemCount;

    @Column(name = "subtotal_amount", nullable = false)
    private Integer subtotalAmount;

    @Column(name = "discount_amount", nullable = false)
    private Integer discountAmount;

    @Column(name = "coupon_discount_amount", nullable = false)
    private Integer couponDiscountAmount;

    @Column(name = "shipping_amount", nullable = false)
    private Integer shippingAmount;

    @Column(name = "rental_amount", nullable = false)
    private Integer rentalAmount;

    @Column(name = "total_amount", nullable = false)
    private Integer totalAmount;

    @Column(name = "points_spent", nullable = false)
    private Integer pointsSpent;

    @Column(name = "points_earned", nullable = false)
    private Integer pointsEarned;

    @Column(name = "applied_coupon_issuance_id")
    private Long appliedCouponIssuanceId;

    @Column(name = "placed_at", nullable = false)
    private LocalDateTime placedAt;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    public static OrderEntity fromModel(OrderAggregate agg) {
        if (agg == null) return null;
        return OrderEntity.builder()
                .id(agg.getId())
                .userId(agg.getUserId())
                .orderNo(agg.getOrderNo())
                .cartId(agg.getCartId())
                .shippingAddressId(agg.getShippingAddressId())
                .status(agg.getStatus() == null ? OrderStatus.PENDING
                        : OrderStatus.valueOf(agg.getStatus().name()))
                .paymentStatus(agg.getPaymentStatus() == null ? PaymentStatus.READY
                        : PaymentStatus.valueOf(agg.getPaymentStatus().name()))
                .itemCount(nz(agg.getItemCount()))
                .subtotalAmount(nz(agg.getSubtotalAmount()))
                .discountAmount(nz(agg.getDiscountAmount()))
                .couponDiscountAmount(nz(agg.getCouponDiscountAmount()))
                .shippingAmount(nz(agg.getShippingAmount()))
                .rentalAmount(nz(agg.getRentalAmount()))
                .totalAmount(nz(agg.getTotalAmount()))
                .appliedCouponIssuanceId(agg.getAppliedCouponIssuanceId())
                .pointsSpent(nz(agg.getPointsSpent()))
                .pointsEarned(nz(agg.getPointsEarned()))
                .placedAt(toLocalDateTimeOrNow(agg.getPlacedAt()))
                .paidAt(toLocalDateTime(agg.getPaidAt()))
                .cancelledAt(toLocalDateTime(agg.getCancelledAt()))
                .completedAt(toLocalDateTime(agg.getCompletedAt()))
                .build();
    }

    public OrderAggregate toDomain(List<OrderItem> items) {
        return OrderAggregate.builder()
                .id(this.id)
                .userId(this.userId)
                .orderNo(this.orderNo)
                .cartId(this.cartId)
                .shippingAddressId(this.shippingAddressId)
                .status(this.status == null ? null
                        : OrderAggregate.Status.valueOf(this.status.name()))
                .paymentStatus(this.paymentStatus == null ? null
                        : OrderAggregate.PaymentStatus.valueOf(this.paymentStatus.name()))
                .itemCount(this.itemCount)
                .subtotalAmount(this.subtotalAmount)
                .discountAmount(this.discountAmount)
                .couponDiscountAmount(this.couponDiscountAmount)
                .shippingAmount(this.shippingAmount)
                .rentalAmount(this.rentalAmount)
                .totalAmount(this.totalAmount)
                .appliedCouponIssuanceId(this.appliedCouponIssuanceId)
                .pointsSpent(this.pointsSpent)
                .pointsEarned(this.pointsEarned)
                .placedAt(toInstant(this.placedAt))
                .paidAt(toInstant(this.paidAt))
                .cancelledAt(toInstant(this.cancelledAt))
                .completedAt(toInstant(this.completedAt))
                .items(items)
                .build();
    }

    private static int nz(Integer v) { return v == null ? 0 : v; }

    private static LocalDateTime toLocalDateTime(Instant instant) {
        return instant == null ? null : LocalDateTime.ofInstant(instant, ZONE_SEOUL);
    }

    private static LocalDateTime toLocalDateTimeOrNow(Instant instant) {
        return instant == null ? LocalDateTime.now(ZONE_SEOUL) : LocalDateTime.ofInstant(instant, ZONE_SEOUL);
    }

    private static Instant toInstant(LocalDateTime ldt) {
        return ldt == null ? null : ldt.atZone(ZONE_SEOUL).toInstant();
    }
}
