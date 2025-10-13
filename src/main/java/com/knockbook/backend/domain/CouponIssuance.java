package com.knockbook.backend.domain;

import lombok.*;

import java.time.Instant;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class CouponIssuance {

    public enum Status { AVAILABLE, USED, EXPIRED, REVOKED }

    private Long id;
    private Long couponId;
    private Long userId;
    private Instant issuedAt;
    private Instant expiresAt;
    private Status status;
    private String code;
    private String name;
    private String type;
    private Integer discountAmount;
    private Integer discountRateBp;
    private Integer maxDiscountAmount;
    private Integer minOrderAmount;
    private String scope;
}
