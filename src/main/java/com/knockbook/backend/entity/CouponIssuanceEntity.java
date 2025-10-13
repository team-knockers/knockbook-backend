package com.knockbook.backend.entity;

import com.knockbook.backend.domain.CouponIssuance;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.time.ZoneId;

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

    public CouponIssuance toDomain() {
        return CouponIssuance.builder()
                .id(id)
                .couponId(couponId)
                .userId(userId)
                .issuedAt(issuedAt != null ? issuedAt.atZone(ZoneId.of("Asia/Seoul")).toInstant() : null)
                .expiresAt(expiresAt != null ? expiresAt.atZone(ZoneId.of("Asia/Seoul")).toInstant() : null)
                .status(CouponIssuance.Status.valueOf(status.name()))
                .build();
    }
}
