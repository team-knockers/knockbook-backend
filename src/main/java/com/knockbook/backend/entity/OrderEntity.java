package com.knockbook.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class OrderEntity {

    public enum OrderStatus { PENDING, FULFILLING, COMPLETED, CANCELLED }

    public enum PaymentStatus { READY, PAID, PARTIAL_REFUNDED, REFUNDED, FAILED, CANCELLED }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "cart_id")
    private Long cartId;

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
}
