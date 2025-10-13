package com.knockbook.backend.dto;

import com.knockbook.backend.domain.CouponIssuance;
import lombok.*;

import java.time.Instant;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class CouponIssuanceResponse {

    private String id;
    private String couponId;
    private String userId;
    private Instant issuedAt;
    private Instant expiresAt;
    private String status;

    private String code;
    private String name;
    private String type;
    private Integer discountAmount;
    private Integer discountRateBp;
    private Integer maxDiscountAmount;
    private Integer minOrderAmount;
    private String scope;

    public static CouponIssuanceResponse fromModel(CouponIssuance m) {
        return CouponIssuanceResponse.builder()
                .id(str(m.getId()))
                .couponId(str(m.getCouponId()))
                .userId(str(m.getUserId()))
                .issuedAt(m.getIssuedAt())
                .expiresAt(m.getExpiresAt())
                .status(m.getStatus().name())
                .code(m.getCode())
                .name(m.getName())
                .type(m.getType())
                .discountAmount(m.getDiscountAmount())
                .discountRateBp(m.getDiscountRateBp())
                .maxDiscountAmount(m.getMaxDiscountAmount())
                .minOrderAmount(m.getMinOrderAmount())
                .scope(m.getScope())
                .build();
    }

    private static String str(Long v) { return v == null ? null : v.toString(); }
}
