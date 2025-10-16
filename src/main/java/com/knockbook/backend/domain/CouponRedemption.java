package com.knockbook.backend.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class CouponRedemption {
    private Long id;
    private Long issuanceId;
    private Long orderId;
    private Integer redeemedAmount;
    private Instant redeemedAt;
}
