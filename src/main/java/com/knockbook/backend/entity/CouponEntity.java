package com.knockbook.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "coupons")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class CouponEntity {

    public enum CouponType { AMOUNT, PERCENT, FREESHIP }
    public enum CouponScope { ALL, PRODUCTS, BOOKS }
    public enum CouponStatus { ACTIVE, PAUSED, EXPIRED }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column
    private String code;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CouponType type;

    @Column(name = "discount_amount")
    private Integer discountAmount; // 원화

    @Column(name = "discount_rate_bp")
    private Integer discountRateBp; // basis points

    @Column(name = "max_discount_amount")
    private Integer maxDiscountAmount;

    @Column(name = "min_order_amount", nullable = false)
    private Integer minOrderAmount;

    @Column(nullable = false)
    private boolean stackable;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CouponScope scope;

    @Column(name = "starts_at", nullable = false)
    private Instant startsAt;

    @Column(name = "ends_at", nullable = false)
    private Instant endsAt;

    @Column(name = "per_user_limit")
    private Integer perUserLimit;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CouponStatus status;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
    private Instant updatedAt;
}
