package com.knockbook.backend.entity;

import com.knockbook.backend.domain.CouponIssuance;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Table(name = "coupon_issuances")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class CouponIssuanceEntity {

    public enum IssuanceStatus { AVAILABLE, USED, EXPIRED, REVOKED }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "coupon_id", nullable = false)
    private Long couponId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "issued_at", nullable = false)
    private LocalDateTime issuedAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IssuanceStatus status;

    public static CouponIssuanceEntity toEntity(CouponIssuance d){
        if (d == null) {
            return null;
        }
        return CouponIssuanceEntity.builder()
                .id(d.getId())
                .couponId(d.getCouponId())
                .userId(d.getUserId())
                .issuedAt(LocalDateTime.from(d.getIssuedAt()))
                .expiresAt(LocalDateTime.from(d.getExpiresAt()))
                .status(CouponIssuanceEntity.IssuanceStatus.valueOf(d.getStatus().name()))
                .build();
    }

    public CouponIssuance toDomain(){
        return CouponIssuance.builder()
                .id(id)
                .couponId(couponId)
                .userId(userId)
                .issuedAt(Instant.from(issuedAt))
                .expiresAt(Instant.from(expiresAt))
                .status(CouponIssuance.Status.valueOf(status.name()))
                .build();
    }
}
