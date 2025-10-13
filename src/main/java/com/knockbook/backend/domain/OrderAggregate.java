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
    private Long cartId;
    private Status status;
    private PaymentStatus paymentStatus;

    private Integer itemCount;
    private Integer subtotalAmount;
    private Integer discountAmount;
    private Integer shippingAmount;
    private Integer rentalAmount;
    private Integer totalAmount;

    private Integer pointsSpent;
    private Integer pointsEarned;

    private Instant placedAt;
    private Instant cancelledAt;
    private Instant completedAt;

    private List<OrderItem> items;
}
