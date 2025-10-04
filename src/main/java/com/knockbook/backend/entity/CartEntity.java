package com.knockbook.backend.entity;

import com.knockbook.backend.domain.Cart;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "carts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class CartEntity {

    public enum Status { OPEN, ORDERED, ABANDONED }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status;

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

    @Column(name = "points_earnable", nullable = false)
    private Integer pointsEarnable;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
    private Instant updatedAt;

}

