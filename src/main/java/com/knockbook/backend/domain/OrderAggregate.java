package com.knockbook.backend.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class OrderAggregate {

    public enum Status { PENDING, FULFILLING, COMPLETED, CANCELLED }
    public enum PaymentStatus { READY, PAID, PARTIAL_REFUNDED, REFUNDED, FAILED, CANCELLED }

    private Long id;
    private Long userId;
    private String orderNo;
    private Long cartId;
    private Status status;
    private PaymentStatus paymentStatus;
    private Long shippingAddressId;

    private Integer itemCount;
    private Integer subtotalAmount;
    private Integer discountAmount;
    private Integer couponDiscountAmount;
    private Integer shippingAmount;
    private Integer rentalAmount;
    private Integer totalAmount;

    private Long appliedCouponIssuanceId;
    private Integer pointsSpent;
    private Integer pointsEarned;

    private Instant placedAt;
    private Instant paidAt;
    private Instant cancelledAt;
    private Instant completedAt;

    private List<OrderItem> items;

    // return a new object with coupons, points, and discounts reset
    public OrderAggregate withDiscountsReset() {
        return OrderAggregate.builder()
                .id(id)
                .orderNo(orderNo)
                .userId(userId)
                .cartId(cartId)
                .status(status)
                .paymentStatus(paymentStatus)
                .itemCount(itemCount)
                .subtotalAmount(subtotalAmount)
                .discountAmount(0)
                .couponDiscountAmount(0)
                .shippingAmount(shippingAmount)
                .rentalAmount(rentalAmount)
                .totalAmount(totalAmount)
                .appliedCouponIssuanceId(null)
                .pointsSpent(0)
                .pointsEarned(0)
                .placedAt(placedAt)
                .paidAt(paidAt)
                .cancelledAt(cancelledAt)
                .completedAt(completedAt)
                .items(items)
                .build();
    }

    // Optionally update status, payment status, and completion time
    public OrderAggregate withStatuses(final Status newStatus,
                                       final PaymentStatus newPaymentStatus,
                                       final Instant newCompletedAt) {
        return OrderAggregate.builder()
                .id(id)
                .orderNo(orderNo)
                .userId(userId)
                .cartId(cartId)
                .shippingAddressId(shippingAddressId)
                .status(newStatus != null ? newStatus : status)
                .paymentStatus(newPaymentStatus != null ? newPaymentStatus : paymentStatus)
                .itemCount(itemCount)
                .subtotalAmount(subtotalAmount)
                .discountAmount(discountAmount)
                .couponDiscountAmount(couponDiscountAmount)
                .shippingAmount(shippingAmount)
                .rentalAmount(rentalAmount)
                .totalAmount(totalAmount)
                .appliedCouponIssuanceId(appliedCouponIssuanceId)
                .pointsSpent(pointsSpent)
                .pointsEarned(pointsEarned)
                .placedAt(placedAt)
                .paidAt(newCompletedAt)
                .cancelledAt(cancelledAt)
                .completedAt(newCompletedAt != null ? newCompletedAt : completedAt)
                .items(items)
                .build();
    }

    // Transition before payment completion (PAID).
    // If completeNow is true, set status to COMPLETED and assign completedAt immediately
    public OrderAggregate paid(final Instant paidAt,
                               final boolean completeNow) {
        var next = this.withStatuses(null, PaymentStatus.PAID, completeNow ? paidAt : completedAt);
        if (completeNow) {
            next = next.withStatuses(Status.COMPLETED, null, paidAt);
        }
        return next;
    }
}
