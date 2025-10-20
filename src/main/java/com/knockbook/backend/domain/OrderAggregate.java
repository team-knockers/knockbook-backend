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
    public enum RentalStatus { PREPARING, SHIPPING, DELIVERED, RETURN_REQUESTED, RETURNING, RETURNED, CANCELLED }

    private Long id;
    private Long userId;
    private String orderNo;
    private Long cartId;
    private Status status;
    private RentalStatus rentalStatus;
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
                .rentalStatus(rentalStatus)
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
                                       final Instant paidAt,
                                       final RentalStatus newRentalStatus) {
        return OrderAggregate.builder()
                .id(id)
                .orderNo(orderNo)
                .userId(userId)
                .cartId(cartId)
                .shippingAddressId(shippingAddressId)
                .status(newStatus != null ? newStatus : status)
                .paymentStatus(newPaymentStatus != null ? newPaymentStatus : paymentStatus)
                .rentalStatus(newRentalStatus != null ? newRentalStatus : rentalStatus)
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
                .paidAt(paidAt)
                .cancelledAt(cancelledAt)
                .completedAt(completedAt)
                .items(items)
                .build();
    }

    public OrderAggregate paid(final Instant paidAt) {
        final var hasRental =
                (rentalAmount != null && rentalAmount > 0)
                        || (items != null && items.stream()
                        .anyMatch(i -> i.getRefType() == OrderItem.RefType.BOOK_RENTAL));

        final var nextRental = hasRental ? RentalStatus.PREPARING : null;
        return this.withStatuses(Status.PENDING, PaymentStatus.PAID, paidAt, nextRental);
    }
}
