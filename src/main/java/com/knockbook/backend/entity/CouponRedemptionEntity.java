package com.knockbook.backend.entity;

import com.knockbook.backend.domain.CouponRedemption;
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

    public static CouponRedemptionEntity toEntity(CouponRedemption d){
        if (d == null) {
            return null;
        }
        return CouponRedemptionEntity.builder()
                .id(d.getId())
                .issuanceId(d.getIssuanceId())
                .orderId(d.getOrderId())
                .redeemedAmount(d.getRedeemedAmount())
                .redeemedAt(d.getRedeemedAt())
                .build();
    }

    public CouponRedemption toDomain() {
        return CouponRedemption.builder()
                .id(id)
                .issuanceId(issuanceId)
                .orderId(orderId)
                .redeemedAmount(redeemedAmount)
                .redeemedAt(redeemedAt)
                .build();
    }
}

