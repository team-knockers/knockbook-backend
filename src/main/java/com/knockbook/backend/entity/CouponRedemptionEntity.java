package com.knockbook.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "coupon_redemptions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class CouponRedemptionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "issuance_id", nullable = false)
    private Long issuanceId;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "redeemed_amount", nullable = false)
    private Integer redeemedAmount;

    @Column(name = "redeemed_at", nullable = false)
    private Instant redeemedAt;
}
